# Maintainer: ZeroXV6 <zeroxv6@example.com>
pkgname=journex
pkgver=1.0.0
pkgrel=1
pkgdesc="A beautiful journaling application with mood tracking, tasks, and more"
arch=('x86_64')
url="https://github.com/zeroxv6/journaling"
license=('MIT')
depends=('java-runtime>=17')
makedepends=('gradle' 'jdk17-openjdk')
source=()
sha256sums=()

build() {
    cd "${startdir}"
    ./gradlew :desktop:packageReleaseUberJarForCurrentOS
}

package() {
    cd "${startdir}"
    
    # Install the JAR file
    install -Dm644 "desktop/build/compose/jars/Journex-linux-x64-1.0.0.jar" \
        "${pkgdir}/usr/share/java/${pkgname}/${pkgname}.jar"
    
    # Create launcher script
    install -Dm755 /dev/stdin "${pkgdir}/usr/bin/${pkgname}" <<EOF
#!/bin/bash
exec java -jar /usr/share/java/${pkgname}/${pkgname}.jar "\$@"
EOF
    
    # Install icon if exists
    if [ -f "desktop/src/jvmMain/resources/journex_icon.png" ]; then
        install -Dm644 "desktop/src/jvmMain/resources/journex_icon.png" \
            "${pkgdir}/usr/share/pixmaps/${pkgname}.png"
    fi
    
    # Create desktop entry
    install -Dm644 /dev/stdin "${pkgdir}/usr/share/applications/${pkgname}.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=Journex
Comment=A beautiful journaling application
Exec=${pkgname}
Icon=${pkgname}
Categories=Office;Utility;
Terminal=false
EOF
}
