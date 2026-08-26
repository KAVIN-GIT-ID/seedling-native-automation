const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const nodemailer = require('nodemailer');
require('dotenv').config();

async function sendReport() {
    const reportPathHtml = path.join(process.cwd(), 'target', 'index.html');
    let reportUrl = process.env.REPORT_URL || `file://${reportPathHtml}`;

    // Auto-deploy to Surge if configured
    if (process.env.SURGE_LOGIN && process.env.SURGE_TOKEN) {
        const timestamp = Date.now();
        const surgeDomain = `seedling-appium-${timestamp}.surge.sh`;
        console.log(`🌐 Deploying report to Surge: https://${surgeDomain} ...`);
        try {
            execSync(`npx surge ./target ${surgeDomain}`, { stdio: 'inherit' });
            reportUrl = `https://${surgeDomain}`;
            console.log(`✅ Successfully deployed report to the web!`);
        } catch (err) {
            console.error('❌ Failed to deploy to Surge:', err.message);
        }
    }

    // Generate fresh HTML Email using generator
    process.env.REPORT_URL = reportUrl;
    try {
        execSync('node utils/generate-email-html.js', { stdio: 'inherit' });
    } catch (e) {
        console.error('Warning: Error generating HTML email body:', e.message);
    }

    const emailHtmlPath = path.join(process.cwd(), 'email-body.html');
    if (!fs.existsSync(emailHtmlPath)) {
        console.error('❌ email-body.html was not generated.');
        process.exit(1);
    }

    const htmlBody = fs.readFileSync(emailHtmlPath, 'utf8');

    // Check if test run had failures
    const xmlReportPath = path.join(process.cwd(), 'target', 'surefire-reports', 'testng-results.xml');
    let hasFailures = false;
    if (fs.existsSync(xmlReportPath)) {
        const xml = fs.readFileSync(xmlReportPath, 'utf8');
        const failMatch = xml.match(/failed="(\d+)"/);
        if (failMatch && parseInt(failMatch[1]) > 0) {
            hasFailures = true;
        }
    }

    // Configure Nodemailer Transport
    const port = parseInt(process.env.SMTP_PORT || '587');
    const transporter = nodemailer.createTransport({
        host: process.env.SMTP_HOST || 'smtp.gmail.com',
        port: port,
        secure: port === 465,
        auth: {
            user: process.env.SMTP_USER,
            pass: process.env.SMTP_PASSWORD,
        },
    });

    try {
        const info = await transporter.sendMail({
            from: `"Seedling Mobile Automation" <${process.env.SMTP_USER}>`,
            to: process.env.RECEIVER_EMAILS || process.env.TL_EMAIL,
            subject: `Seedling Mobile Test Run Summary: ${hasFailures ? '❌ FAILED' : '✅ PASSED'}`,
            html: htmlBody,
        });
        console.log('✅ Email sent successfully! Message ID:', info.messageId);
    } catch (error) {
        console.error('❌ Error sending email:', error);
        process.exit(1);
    }
}

sendReport();
