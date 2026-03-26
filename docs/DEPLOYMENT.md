# Deploying Journex Website to GitHub Pages

## Step 1: Prepare Your Repository

1. Make sure all your website files are in the `website` folder
2. Rename `home.html` to `index.html` (GitHub Pages requires this as the entry point):
   ```bash
   cd website
   mv home.html index.html
   ```

3. Update links in `privacy.html` and `terms.html`:
   - Change `home.html` back to `index.html`

## Step 2: Push to GitHub

```bash
git add website/
git commit -m "Add website for GitHub Pages"
git push origin main
```

## Step 3: Enable GitHub Pages

**Important**: GitHub Pages only allows `/` (root) or `/docs` as source folders. Since your files are in `/website`, you have 3 options:

### Option A: Use GitHub Actions (Recommended - Keep website folder)

1. The workflow file `.github/workflows/deploy-pages.yml` is already created
2. Go to your repository **Settings** → **Pages**
3. Under **Source**, select: **GitHub Actions**
4. Push your changes:
   ```bash
   git add .
   git commit -m "Add GitHub Pages deployment"
   git push origin main
   ```
5. The site will automatically deploy from the `website` folder

### Option B: Rename to docs folder

```bash
mv website docs
git add .
git commit -m "Rename website to docs for GitHub Pages"
git push origin main
```

Then in Settings → Pages → Source: `main` branch, `/docs` folder

### Option C: Move to root (Not recommended - mixes website with app code)

```bash
mv website/* .
git add .
git commit -m "Move website to root"
git push
```

Then in Settings → Pages → Source: `main` branch, `/` (root)

**Recommended**: Use Option A (GitHub Actions) - it's the cleanest solution.

Your site will be live at: `https://zeroxv6.github.io/Journex/`

## Step 4: Add Custom Domain (Optional)

### A. Configure DNS at Your Domain Provider

You have two options:

#### Option 1: Apex Domain (example.com)
Add these A records to your DNS:
```
Type: A
Name: @
Value: 185.199.108.153

Type: A
Name: @
Value: 185.199.109.153

Type: A
Name: @
Value: 185.199.110.153

Type: A
Name: @
Value: 185.199.111.153
```

#### Option 2: Subdomain (www.example.com or journex.example.com)
Add a CNAME record:
```
Type: CNAME
Name: www (or journex)
Value: zeroxv6.github.io
```

### B. Configure GitHub Pages

1. In your repository, go to **Settings** → **Pages**
2. Under **Custom domain**, enter your domain (e.g., `journex.yourdomain.com`)
3. Click **Save**
4. **GitHub will automatically create a CNAME file** in your website folder
5. Wait for DNS check to complete (can take up to 24 hours)
6. Once verified, check **Enforce HTTPS**

**Note**: You don't need to manually create the CNAME file - GitHub creates it automatically when you enter your custom domain in Settings. The CNAME file is just for GitHub Pages to know which domain to serve, while your domain provider's DNS settings point the domain to GitHub's servers.

## Step 5: Verify Deployment

1. Wait 2-5 minutes for GitHub to build and deploy
2. Visit your site at `https://zeroxv6.github.io/Journex/`
3. If using custom domain, visit `https://yourdomain.com`

## Troubleshooting

### Site not loading?
- Check that `index.html` exists in the `website` folder
- Verify GitHub Pages is enabled in Settings
- Check the Actions tab for build errors

### Custom domain not working?
- Verify DNS records are correct using `dig yourdomain.com` or `nslookup yourdomain.com`
- DNS propagation can take 24-48 hours
- Make sure CNAME file contains only the domain (no https://)

### 404 errors on subpages?
- Make sure all links use relative paths
- Check file names are correct (case-sensitive on Linux servers)

## Updating Your Site

After making changes:
```bash
git add website/
git commit -m "Update website"
git push
```

GitHub Pages will automatically rebuild and deploy (takes 1-2 minutes).

## Free SSL Certificate

GitHub Pages provides free SSL certificates automatically via Let's Encrypt. Just enable "Enforce HTTPS" in Settings → Pages after your custom domain is verified.

## Popular Domain Providers

- **Namecheap**: Easy DNS management, affordable
- **Cloudflare**: Free DNS + CDN + DDoS protection
- **Google Domains**: Simple interface
- **Porkbun**: Cheap and reliable

## Using Cloudflare (Recommended)

Cloudflare provides free CDN, SSL, and DDoS protection:

1. Sign up at cloudflare.com
2. Add your domain
3. Update nameservers at your domain registrar
4. Add DNS records as described above
5. Enable "Proxied" (orange cloud) for CDN benefits
6. Set SSL/TLS mode to "Full"

This gives you:
- Faster loading times worldwide
- Additional security
- Analytics
- Free SSL certificate
