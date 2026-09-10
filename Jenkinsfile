pipeline {
    agent any

    tools {
        maven 'maven-3.9.11'
        nodejs 'NodeJS-20-LTS'
    }

    parameters {
        choice(
            name: 'PLATFORM',
            choices: ['android', 'ios'],
            description: 'Target Mobile Platform'
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
        string(
            name: 'GDRIVE_FILE_ID',
            defaultValue: '1dDV9FrZieZXvqcirdp5Brm_pPrYzu1Mf',
            description: 'Google Drive File ID or Share URL for Android APK (leave as default or paste new ID/URL whenever build changes)'
        )
        booleanParam(
            name: 'FORCE_FRESH_APP',
            defaultValue: false,
            description: 'Force re-download fresh APK from Google Drive (bypasses cache)'
        )
    }

    environment {
        ANDROID_HOME = '/var/lib/jenkins/android-sdk'
        PATH = "/var/lib/jenkins/android-sdk/emulator:/var/lib/jenkins/android-sdk/platform-tools:/var/lib/jenkins/android-sdk/cmdline-tools/latest/bin:${env.PATH}"
        QA_USERNAME = 'kavinap@uit.ac.in'
        QA_PASSWORD = 'Uit@1234567890'
        SURGE_LOGIN = 'kavinap@uit.ac.in'
        SURGE_TOKEN = '8d9007929b91f647f65f8f3667da6ae0'
        EMAIL_USER = 'apkavin483@gmail.com'
        EMAIL_PASS = 'cjhhgojueofrqntp'
        TL_EMAIL   = 'kavinap@uit.ac.in'
    }

    stages {
        stage('Initialize & Clean') {
            steps {
                sh '''
                    echo "=== ENVIRONMENT INFO ==="
                    echo "Selected Platform: ${PLATFORM}"
                    echo "Selected Component: ${COMPONENT}"
                    echo "Google Drive APK ID: ${GDRIVE_FILE_ID}"
                    echo "Java Version:" && java -version
                    echo "Maven Version:" && mvn -v
                    echo "Node Version:" && node -v
                    echo "NPM Version:" && npm -v
                    if [ "${PLATFORM}" = "android" ]; then
                        echo "ADB Version:" && adb --version
                    fi
                    
                    # Clean previous run artifacts
                    rm -rf logs target appium.log emulator.log email-body.html
                    mkdir -p logs apps/qa config target/videos
                '''
            }
        }

        stage('Setup Test Credentials') {
            steps {
                sh '''
                    echo "qa.username=${QA_USERNAME}" > config/credentials.properties
                    echo "qa.password=${QA_PASSWORD}" >> config/credentials.properties
                    echo "prod.username=" >> config/credentials.properties
                    echo "prod.password=" >> config/credentials.properties
                    echo "ios.devicePasscode=000000" >> config/credentials.properties
                    echo "Credentials configured successfully."
                '''
            }
        }

        stage('Appium & Driver Setup') {
            steps {
                sh '''
                    if ! command -v appium &> /dev/null; then
                        echo "Installing Appium..."
                        npm install -g appium --quiet
                    fi
                    
                    echo "Appium version: $(appium -v)"

                    if [ "${PLATFORM}" = "android" ]; then
                        if ! appium driver list --installed | grep -q "uiautomator2"; then
                            echo "Installing UiAutomator2 driver..."
                            appium driver install uiautomator2 || true
                        fi
                    elif [ "${PLATFORM}" = "ios" ]; then
                        if ! appium driver list --installed | grep -q "xcuitest"; then
                            echo "Installing XCUITest driver..."
                            appium driver install xcuitest || true
                        fi
                    fi

                    appium driver list --installed
                '''
            }
        }

        stage('Download / Cache Application') {
            steps {
                sh '''
                    mkdir -p apps/qa
                    
                    if [ "${PLATFORM}" = "android" ]; then
                        # Extract raw file ID if user pasted a full Google Drive URL
                        RAW_ID=$(echo "${GDRIVE_FILE_ID}" | grep -oE "d/[a-zA-Z0-9_-]+" | cut -d'/' -f2 || true)
                        if [ -z "$RAW_ID" ]; then
                            RAW_ID=$(echo "${GDRIVE_FILE_ID}" | grep -oE "id=[a-zA-Z0-9_-]+" | cut -d'=' -f2 || true)
                        fi
                        if [ -z "$RAW_ID" ]; then
                            RAW_ID="${GDRIVE_FILE_ID}"
                        fi
                        echo "Resolved Google Drive ID: $RAW_ID"

                        if [ "${FORCE_FRESH_APP}" = "true" ] || ! ls apps/qa/*.apk apps/*.apk 1> /dev/null 2>&1; then
                            echo "Downloading QA APK from Google Drive ID: $RAW_ID..."
                            if ! command -v gdown &> /dev/null; then
                                npm install -g gdown || true
                            fi
                            gdown "$RAW_ID" -O apps/qa/seedling-dev.apk || \
                            curl -s -L "https://drive.google.com/uc?export=download&id=${RAW_ID}" -o apps/qa/seedling-dev.apk
                            echo "APK download complete."
                        else
                            echo "Using existing cached APK in apps folder."
                        fi
                    elif [ "${PLATFORM}" = "ios" ]; then
                        # Unpack iOS App if zipped
                        for zipfile in apps/qa/*.zip apps/*.zip; do
                            if [ -f "$zipfile" ]; then
                                echo "Unpacking $zipfile into apps/qa/..."
                                unzip -o "$zipfile" -d apps/qa/
                                if [ -d apps/qa/Payload ]; then
                                    mv apps/qa/Payload/*.app apps/qa/ 2>/dev/null || true
                                fi
                            fi
                        done
                    fi

                    ls -la apps/qa/
                '''
            }
        }

        stage('Start Headless Emulator / Simulator & Appium') {
            steps {
                sh '''
                    # 1. Clean old processes
                    pkill -f appium 2>/dev/null || true

                    # 2. Launch Appium Server
                    echo "Starting Appium Server on port 4723..."
                    nohup appium --log appium.log --port 4723 > /dev/null 2>&1 &
                    sleep 10
                    curl --retry 5 --retry-delay 2 --retry-connrefused http://127.0.0.1:4723/status || echo "Appium warming up..."

                    # 3. Platform-specific device boot
                    if [ "${PLATFORM}" = "android" ]; then
                        adb emu kill 2>/dev/null || true
                        killall -9 qemu-system-x86_64 2>/dev/null || true
                        sleep 2

                        echo "Starting Android Emulator (testDevice)..."
                        nohup emulator -avd testDevice -no-window -no-snapshot-save -noaudio -no-boot-anim -gpu swiftshader_indirect -memory 3072 > emulator.log 2>&1 &
                        
                        echo "Waiting for device to register in ADB..."
                        adb wait-for-device
                        
                        echo "Waiting for Android system to complete boot..."
                        while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
                            sleep 3
                        done
                        echo "Android emulator successfully booted!"
                        sleep 10

                        # Dismiss system dialogs & configure settings
                        adb shell input keyevent 4 || true
                        adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS || true
                        adb shell settings put secure immersive_mode_confirmations confirmed || true

                        # Pre-install APK & warm up React Native bundle
                        APK_PATH=$(find apps/qa apps -name "*.apk" 2>/dev/null | head -1)
                        echo "Pre-installing APK: $APK_PATH"
                        adb install -r "$APK_PATH" || true

                        PACKAGE="com.seedling.dev"
                        ACTIVITY="com.seedling.dev.MainActivity"
                        echo "Warming up React Native app..."
                        adb shell am start -n "$PACKAGE/$ACTIVITY" || true
                        sleep 25
                        
                        # Diagnostic snapshot
                        mkdir -p logs
                        adb shell screencap -p /sdcard/pre_test.png || true
                        adb pull /sdcard/pre_test.png logs/pre_test.png || true
                        
                        # Stop app ready for fresh Appium test launch
                        adb shell am force-stop "$PACKAGE" || true
                        sleep 2

                    elif [ "${PLATFORM}" = "ios" ]; then
                        echo "Configuring iOS Simulator..."
                        UDID=$(xcrun simctl list devices available 2>/dev/null | grep "iPhone 15" | head -1 | grep -oE "\([0-9A-F-]+\)" | tr -d "()" || true)
                        if [ -z "$UDID" ]; then
                            UDID=$(xcrun simctl list devices available 2>/dev/null | grep "iPhone" | head -1 | grep -oE "\([0-9A-F-]+\)" | tr -d "()" || true)
                        fi
                        if [ -n "$UDID" ]; then
                            echo "Booting iOS Simulator UDID: $UDID"
                            xcrun simctl boot $UDID || true
                            xcrun simctl bootstatus $UDID -b || true
                            for appdir in apps/qa/*.app apps/*.app; do
                                if [ -d "$appdir" ]; then
                                    echo "Pre-installing $appdir into simulator $UDID..."
                                    xcrun simctl install $UDID "$appdir" || true
                                fi
                            done
                        else
                            echo "Notice: xcrun not detected on this agent (Ubuntu). Ensure tests target connected remote iOS device or run on Mac agent."
                        fi
                    fi
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
                    
                    echo "Executing Command: $CMD"
                    $CMD || true
                '''
            }
        }

        stage('Deploy Report & Video to Surge') {
            steps {
                sh '''
                    if [ -f target/index.html ]; then
                        echo "Deploying Extent Report + Videos to Surge..."
                        DOMAIN="jenkins-${BUILD_NUMBER}-seedling-mobile-qa.surge.sh"
                        npx surge ./target "$DOMAIN" --token "$SURGE_TOKEN" || true
                        echo "REPORT_URL=https://${DOMAIN}" > surge.env
                    else
                        echo "REPORT_URL=#" > surge.env
                    fi
                    cat surge.env
                '''
            }
        }

        stage('Generate Dynamic Email Report') {
            steps {
                sh '''
                    source surge.env || true
                    export REPORT_URL="${REPORT_URL:-#}"
                    export RUN_URL="${BUILD_URL}"
                    export ENVIRONMENT="QA"
                    export PLATFORM="${PLATFORM}"
                    export COMPONENT="${COMPONENT}"
                    export JOB_STATUS="success"
                    export BRANCH_NAME="main"
                    
                    if grep -q 'status="FAIL"' target/surefire-reports/testng-results.xml 2>/dev/null; then
                        export JOB_STATUS="failure"
                    fi

                    node utils/generate-email-html.js
                '''
            }
        }

        stage('Send Email Notification') {
            steps {
                sh '''
                    node -e '
                    const nodemailer = require("nodemailer");
                    const fs = require("fs");

                    async function main() {
                        const htmlContent = fs.readFileSync("email-body.html", "utf8");
                        const transporter = nodemailer.createTransport({
                            host: "smtp.gmail.com",
                            port: 465,
                            secure: true,
                            auth: {
                                user: process.env.EMAIL_USER,
                                pass: process.env.EMAIL_PASS
                            }
                        });

                        const info = await transporter.sendMail({
                            from: `"Seedling Native Automation" <${process.env.EMAIL_USER}>`,
                            to: process.env.TL_EMAIL,
                            subject: `Jenkins Mobile QA [${process.env.PLATFORM.toUpperCase()}] - Test Execution Report`,
                            html: htmlContent
                        });

                        console.log("Email sent successfully: %s", info.messageId);
                    }
                    main().catch(err => {
                        console.error("Failed to send email:", err.message);
                    });
                    ' || true
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "=== TEARDOWN: Stopping Emulator & Appium ==="
                if [ "${PLATFORM}" = "android" ]; then
                    adb emu kill 2>/dev/null || true
                fi
                pkill -f appium 2>/dev/null || true
            '''
            
            archiveArtifacts artifacts: 'logs/**, target/videos/**, appium.log, emulator.log, target/surefire-reports/**', allowEmptyArchive: true

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
    }
}
