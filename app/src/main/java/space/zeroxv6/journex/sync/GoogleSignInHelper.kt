package space.zeroxv6.journex.sync
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
object GoogleSignInHelper {
    private const val TAG = "GoogleSignInHelper"
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        return getLastSignedInAccount(context)
    }
    fun isSignedIn(context: Context): Boolean {
        return getLastSignedInAccount(context) != null
    }
    fun getSignInIntent(context: Context): Intent {
        return getGoogleSignInClient(context).signInIntent
    }
    fun signOut(context: Context, onComplete: (() -> Unit)? = null) {
        getGoogleSignInClient(context).signOut().addOnCompleteListener {
            onComplete?.invoke()
        }
    }
    fun handleSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            Result.success(account)
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-in failed with status code: ${e.statusCode}", e)
            val errorMessage = when (e.statusCode) {
                10 -> "Developer error: Please ensure SHA-1 fingerprint is registered in Firebase Console and OAuth client is properly configured"
                12501 -> "Sign-in cancelled by user"
                12500 -> "Sign-in failed: Please try again"
                7 -> "Network error: Please check your internet connection"
                else -> "Sign-in failed with error code: ${e.statusCode}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign-in", e)
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}
