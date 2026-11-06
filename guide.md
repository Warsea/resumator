# Resumetor — Setup, Development & Cross-Platform Packaging Guide

This guide covers how to set up **Resumetor** from scratch on a machine that does **not** have Java or Maven installed yet, how to run it in development mode, and how to package standalone native installers/executables for **Linux**, **Windows**, and **macOS**.

---

## 1. Prerequisites (Installing Java 21, Maven & Git)

Resumetor requires **JDK 21** and **Apache Maven**. Follow the instructions below for your operating system.

### Linux (Ubuntu / Debian)

Open a terminal and run:

```bash
sudo apt update
sudo apt install -y git openjdk-21-jdk maven fakeroot binutils xdg-utils
```

Verify installation:
```bash
java -version
mvn -v
```

---

### Windows (Windows 10 / 11)

Open **PowerShell** (or Windows Terminal) as Administrator and use Windows's built-in `winget` package manager:

```powershell
# 1. Install Git
winget install --id Git.Git -e

# 2. Install Eclipse Temurin JDK 21
winget install --id EclipseAdoptium.Temurin.21.JDK -e

# 3. Install Apache Maven
winget install --id Apache.Maven -e
```

> **Important:** Close and reopen PowerShell after installing so your `PATH` environment variable refreshes.

Verify installation:
```powershell
java -version
mvn -v
```

---

### macOS (Intel & Apple Silicon)

Open **Terminal** and install via [Homebrew](https://brew.sh/) (if you don't have Homebrew, install it first from `https://brew.sh`):

```bash
# 1. Install Git, Temurin JDK 21, and Maven
brew install git maven
brew install --cask temurin@21
```

Verify installation:
```bash
java -version
mvn -v
```

---

## 2. Cloning & Running in Development Mode (All Platforms)

### Step 1: Clone the repository

```bash
git clone <YOUR_GITHUB_REPO_URL>
cd resumetor
```

### Step 2: Configure your OpenRouter API Key

Either create a `.env` file in the project root:

**Linux / macOS:**
```bash
echo "OPENROUTER_API_KEY=your_openrouter_api_key_here" > .env
```

**Windows (PowerShell):**
```powershell
Set-Content -Path .env -Value "OPENROUTER_API_KEY=your_openrouter_api_key_here"
```

*(Or hardcode the fallback key on line 33 of `src/main/java/app/resumetor/ai/OpenRouterLLMService.java` before building standalone installers).*

### Step 3: Run the application

```bash
mvn clean javafx:run
```

> Maven automatically detects your OS (`linux`, `win`, or `mac`) and downloads the matching native JavaFX libraries.

---

## 3. Packaging Standalone Native Applications (`jpackage`)

JDK 21 includes `jpackage`, which bundles your application along with a stripped-down Java Runtime (`jlink`). **End users do not need Java installed to run the packaged app.**

> **Note:** `jpackage` builds packages for the OS it is currently running on:
> * Run on **Linux** to produce `.deb` / `.rpm` / Linux portable folders.
> * Run on **Windows** to produce `.exe` / `.msi` / Windows portable folders.
> * Run on **macOS** to produce `.dmg` / `.pkg` / `.app` bundles.

---

### A. Packaging on Linux (`.deb` Installer or Portable App)

#### 1. Compile and stage dependencies
```bash
mvn clean package -DskipTests
mvn dependency:copy-dependencies -DoutputDirectory=target/libs
cp target/resumetor-1.0-SNAPSHOT.jar target/libs/
```

#### 2. Build a `.deb` installer (Ubuntu / Debian)
```bash
rm -f dist/linux/*.deb

jpackage \
  --type deb \
  --name resumetor \
  --app-version 1.0.0 \
  --input target/libs \
  --main-jar resumetor-1.0-SNAPSHOT.jar \
  --main-class app.resumetor.Launcher \
  --icon src/main/resources/images/icon.png \
  --linux-shortcut \
  --linux-menu-group "Office;Utility;" \
  --dest dist/linux
```

* **Output:** `dist/linux/resumetor_1.0.0_amd64.deb`
* **Install:** `sudo dpkg -i dist/linux/resumetor_1.0.0_amd64.deb`
* **Uninstall:** `sudo apt purge -y resumetor`

*(Tip: If you want a portable Linux folder instead of a `.deb`, change `--type deb` to `--type app-image` and remove the `--linux-*` flags).*

---

### B. Packaging on Windows (Portable `.exe` or Setup Installer)

#### 1. Compile and stage dependencies (PowerShell)
```powershell
mvn clean package -DskipTests
mvn dependency:copy-dependencies -DoutputDirectory=target/libs
Copy-Item target/resumetor-1.0-SNAPSHOT.jar target/libs/
```

#### 2. Option 1: Portable Windows App Folder (No extra tools needed)
```powershell
jpackage `
  --type app-image `
  --name Resumetor `
  --input target/libs `
  --main-jar resumetor-1.0-SNAPSHOT.jar `
  --main-class app.resumetor.Launcher `
  --icon src/main/resources/images/icon.ico `
  --dest dist/windows
```

* **Output:** `dist\windows\Resumetor\Resumetor.exe`
* You can zip the `dist\windows\Resumetor` folder and share it with any Windows user.

#### 3. Option 2: Windows `.exe` / `.msi` Setup Wizard (Start Menu & Desktop Shortcut)
Building an installer wizard on Windows requires **WiX Toolset v3**:

```powershell
# Install WiX Toolset (close & reopen PowerShell after installing)
winget install --id FireGiant.WiXToolset -e

# Build the .exe installer
jpackage `
  --type exe `
  --name Resumetor `
  --app-version 1.0.0 `
  --input target/libs `
  --main-jar resumetor-1.0-SNAPSHOT.jar `
  --main-class app.resumetor.Launcher `
  --icon src/main/resources/images/icon.ico `
  --win-shortcut `
  --win-menu `
  --dest dist/windows
```

* **Output:** `dist\windows\Resumetor-1.0.0.exe`

---

### C. Packaging on macOS (`.dmg` Disk Image or `.app` Bundle)

#### 1. Compile and stage dependencies
```bash
mvn clean package -DskipTests
mvn dependency:copy-dependencies -DoutputDirectory=target/libs
cp target/resumetor-1.0-SNAPSHOT.jar target/libs/
```

#### 2. (Optional) Convert `icon.png` to macOS `.icns`
macOS uses `.icns` for app icons (or you can omit `--icon` to use the default icon):
```bash
mkdir -p icon.iconset
sips -z 512 512 src/main/resources/images/icon.png --out icon.iconset/icon_512x512.png
iconutil -c icns icon.iconset -o src/main/resources/images/icon.icns
rm -rf icon.iconset
```

#### 3. Build a `.dmg` installer
```bash
jpackage \
  --type dmg \
  --name Resumetor \
  --app-version 1.0.0 \
  --input target/libs \
  --main-jar resumetor-1.0-SNAPSHOT.jar \
  --main-class app.resumetor.Launcher \
  --icon src/main/resources/images/icon.icns \
  --dest dist/mac
```

* **Output:** `dist/mac/Resumetor-1.0.0.dmg` (or use `--type app-image` to generate `dist/mac/Resumetor.app` directly).

---

## 4. Where Data is Stored

* **Development mode (`mvn javafx:run`):** Uses `./data/resumetor.db` in the project directory if `./data` exists and is writable.
* **Installed app (`.deb`, `.exe`, `.dmg`):** Automatically stores the SQLite database in your user home folder at:
  * **Linux / macOS:** `~/.resumetor/resumetor.db`
  * **Windows:** `C:\Users\<YourUsername>\.resumetor\resumetor.db`
