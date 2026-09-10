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
            description: ''
        )
        choice(
            name: 'COMPONENT',
            choices: [
                'all (runs all QA tests)',
                'com.company.automation.tests.LoginTests',
                'com.company.automation.tests.SeedlingCreationTests'
            ],
            description: ''
        )
        string(
            name: 'GDRIVE_FILE_ID',
            defaultValue: '1dDV9FrZieZXvqcirdp5Brm_pPrYzu1Mf',
            description: ''
        )
        booleanParam(
            name: 'FORCE_FRESH_APP',
            defaultValue: false,
            description: ''
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
                        RAW_ID="${GDRIVE_FILE_ID}"
                        case "$RAW_ID" in
                            *d/*)
                                RAW_ID=$(echo "$RAW_ID" | sed -n 's|.*d/\\([^/]*\\).*|\\1|p')
                                ;;
                            *id=*)
                                RAW_ID=$(echo "$RAW_ID" | sed -n 's|.*id=\\([^&]*\\).*|\\1|p')
                                ;;
                        esac
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
                    pkill -f appium 2>/dev/null || true

                    echo "Starting Appium Server on port 4723..."
                    nohup appium --log appium.log --port 4723 > /dev/null 2>&1 &
                    sleep 10
                    curl --retry 5 --retry-delay 2 --retry-connrefused http://127.0.0.1:4723/status || echo "Appium warming up..."

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

                        adb shell input keyevent 4 || true
                        adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS || true
                        adb shell settings put secure immersive_mode_confirmations confirmed || true

                        APK_PATH=$(find apps/qa apps -name "*.apk" 2>/dev/null | head -1)
                        echo "Pre-installing APK: $APK_PATH"
                        adb install -r "$APK_PATH" || true

                        PACKAGE="com.seedling.dev"
                        ACTIVITY="com.seedling.dev.MainActivity"
                        echo "Warming up React Native app..."
                        adb shell am start -n "$PACKAGE/$ACTIVITY" || true
                        sleep 25
                        
                        mkdir -p logs
                        adb shell screencap -p /sdcard/pre_test.png || true
                        adb pull /sdcard/pre_test.png logs/pre_test.png || true
                        
                        adb shell am force-stop "$PACKAGE" || true
                        sleep 2

                    elif [ "${PLATFORM}" = "ios" ]; then
                        echo "Configuring iOS Simulator..."
                        if command -v xcrun &> /dev/null; then
                            UDID=$(xcrun simctl list devices available | awk -F '[()]' '/iPhone 15/{print $2; exit}')
                            if [ -z "$UDID" ]; then
                                UDID=$(xcrun simctl list devices available | awk -F '[()]' '/iPhone/{print $2; exit}')
                            fi
                            if [ -n "$UDID" ]; then
                                echo "Booting iOS Simulator UDID: $UDID"
                                xcrun simctl boot "$UDID" || true
                                xcrun simctl bootstatus "$UDID" -b || true
                                for appdir in apps/qa/*.app apps/*.app; do
                                    if [ -d "$appdir" ]; then
                                        echo "Pre-installing $appdir into simulator $UDID..."
                                        xcrun simctl install "$UDID" "$appdir" || true
                                    fi
                                done
                            fi
                        else
                            echo "Notice: xcrun not available on Linux. iOS requires macOS runner."
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
                    npm install nodemailer --no-save --quiet || true
                    node utils/send-email.js || true
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
