pipeline {
    agent any

    tools {
        maven 'maven-3.9.11'
        nodejs 'NodeJS-20-LTS'
    }

    parameters {
        choice(
            name: 'PLATFORM',
            choices: ['android'],
            description: 'Target mobile platform'
        )
        choice(
            name: 'COMPONENT',
            choices: [
                'all (runs all QA tests)',
                'com.company.automation.tests.LoginTests',
                'com.company.automation.tests.SeedlingCreationTests'
            ],
            description: 'Test file to run on QA'
        )
        booleanParam(
            name: 'FORCE_FRESH_APP',
            defaultValue: false,
            description: 'Force re-download fresh APK from Google Drive'
        )
    }

    environment {
        ANDROID_HOME = '/var/lib/jenkins/android-sdk'
        PATH = "/var/lib/jenkins/android-sdk/emulator:/var/lib/jenkins/android-sdk/platform-tools:/var/lib/jenkins/android-sdk/cmdline-tools/latest/bin:${env.PATH}"
        QA_USERNAME = credentials('QA_USERNAME') // Can fallback to default if not configured in Jenkins credentials
        QA_PASSWORD = credentials('QA_PASSWORD')
    }

    stages {
        stage('Initialize & Clean') {
            steps {
                sh '''
                    echo "=== ENVIRONMENT INFO ==="
                    echo "Java Version:" && java -version
                    echo "Maven Version:" && mvn -v
                    echo "Node Version:" && node -v
                    echo "NPM Version:" && npm -v
                    echo "ADB Version:" && adb --version
                    
                    # Clean previous run artifacts
                    rm -rf logs target appium.log emulator.log
                    mkdir -p logs apps/qa config
                '''
            }
        }

        stage('Setup Test Credentials') {
            steps {
                sh '''
                    # Populate credentials if provided or preserve defaults
                    USER_VAL="${QA_USERNAME:-kavinap@uit.ac.in}"
                    PASS_VAL="${QA_PASSWORD:-Uit@1234567890}"
                    
                    echo "qa.username=${USER_VAL}" > config/credentials.properties
                    echo "qa.password=${PASS_VAL}" >> config/credentials.properties
                    echo "prod.username=" >> config/credentials.properties
                    echo "prod.password=" >> config/credentials.properties
                    echo "ios.devicePasscode=000000" >> config/credentials.properties
                    echo "Credentials configured."
                '''
            }
        }

        stage('Appium & Driver Setup') {
            steps {
                sh '''
                    # Ensure appium and uiautomator2 are installed
                    if ! command -v appium &> /dev/null; then
                        echo "Installing Appium..."
                        npm install -g appium --quiet
                    fi
                    
                    echo "Appium version: $(appium -v)"
                    
                    if ! appium driver list --installed | grep -q "uiautomator2"; then
                        echo "Installing UiAutomator2 driver..."
                        appium driver install uiautomator2 || true
                    fi
                    appium driver list --installed
                '''
            }
        }

        stage('Download / Cache QA APK') {
            steps {
                sh '''
                    mkdir -p apps/qa
                    
                    if [ "${FORCE_FRESH_APP}" = "true" ] || ! ls apps/qa/*.apk apps/*.apk 1> /dev/null 2>&1; then
                        echo "Downloading QA APK from Google Drive..."
                        if ! command -v gdown &> /dev/null; then
                            npm install -g gdown || true
                        fi
                        
                        # Attempt gdown or direct curl fallback
                        gdown 1dDV9FrZieZXvqcirdp5Brm_pPrYzu1Mf -O apps/qa/seedling-dev.apk || \
                        curl -s -L "https://drive.google.com/uc?export=download&id=1dDV9FrZieZXvqcirdp5Brm_pPrYzu1Mf" -o apps/qa/seedling-dev.apk
                        echo "APK download complete."
                    else
                        echo "Using existing cached APK in apps folder."
                    fi
                    ls -la apps/qa/
                '''
            }
        }

        stage('Start Headless Emulator & Appium') {
            steps {
                sh '''
                    # 1. Kill any stale emulator or appium instances
                    adb emu kill 2>/dev/null || true
                    killall -9 qemu-system-x86_64 2>/dev/null || true
                    pkill -f appium 2>/dev/null || true
                    sleep 2

                    # 2. Launch Appium server in background
                    echo "Starting Appium Server..."
                    nohup appium --log appium.log --port 4723 > /dev/null 2>&1 &
                    sleep 10
                    curl --retry 5 --retry-delay 2 --retry-connrefused http://127.0.0.1:4723/status || echo "Appium still warming..."

                    # 3. Launch Headless Android Emulator
                    echo "Starting Android Emulator (testDevice)..."
                    nohup emulator -avd testDevice -no-window -no-snapshot-save -noaudio -no-boot-anim -gpu swiftshader_indirect -memory 3072 > emulator.log 2>&1 &
                    
                    echo "Waiting for device to be online in ADB..."
                    adb wait-for-device
                    
                    echo "Waiting for Android boot completion..."
                    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
                        sleep 3
                    done
                    echo "Android emulator successfully booted!"
                    sleep 10

                    # 4. Dismiss system dialogs & configure settings
                    adb shell input keyevent 4 || true
                    adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS || true
                    adb shell settings put secure immersive_mode_confirmations confirmed || true

                    # 5. Pre-install APK & warm up React Native bundle
                    APK_PATH=$(find apps/qa apps -name "*.apk" 2>/dev/null | head -1)
                    echo "Pre-installing APK: $APK_PATH"
                    adb install -r "$APK_PATH" || true

                    PACKAGE="com.seedling.dev"
                    ACTIVITY="com.seedling.dev.MainActivity"
                    echo "Warming up React Native app..."
                    adb shell am start -n "$PACKAGE/$ACTIVITY" || true
                    sleep 25
                    
                    # Capture diagnostic screenshot
                    adb shell screencap -p /sdcard/pre_test.png || true
                    adb pull /sdcard/pre_test.png logs/pre_test.png || true
                    
                    # Stop app ready for test launch
                    adb shell am force-stop "$PACKAGE" || true
                    sleep 2
                '''
            }
        }

        stage('Execute Mobile Tests') {
            steps {
                sh '''
                    if [[ "${COMPONENT}" == *"all"* ]]; then
                        CMD="mvn test -Denv=qa -Dplatform=${PLATFORM} -Dexecution=ci -DsuiteXmlFile=testng-qa.xml"
                    else
                        CMD="mvn test -Denv=qa -Dplatform=${PLATFORM} -Dexecution=ci -Dtest=${COMPONENT}"
                    fi
                    
                    echo "Executing: $CMD"
                    $CMD
                '''
            }
        }
    }

    post {
        always {
            // Teardown processes to prevent zombie runners
            sh '''
                echo "=== TEARDOWN: Stopping Emulator & Appium ==="
                adb emu kill 2>/dev/null || true
                pkill -f appium 2>/dev/null || true
            '''
            
            // Archive logs and test outputs
            archiveArtifacts artifacts: 'logs/**, appium.log, emulator.log, target/surefire-reports/**', allowEmptyArchive: true

            // Publish HTML Reports
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'index.html',
                reportName: 'Extent Test Report',
                reportTitles: 'Seedling Mobile Automation Report'
            ])
        }
        success {
            echo "✅ All QA Mobile tests executed and passed successfully!"
        }
        failure {
            echo "❌ Some QA Mobile tests failed. Please inspect the Extent Report or surefire-reports."
        }
    }
}
