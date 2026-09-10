const fs = require('fs');
const nodemailer = require('nodemailer');

async function sendMail() {
    const htmlPath = 'email-body.html';
    if (!fs.existsSync(htmlPath)) {
        console.log('No email-body.html found to send.');
        return;
    }

    const htmlContent = fs.readFileSync(htmlPath, 'utf8');
    const user = process.env.EMAIL_USER;
    const pass = process.env.EMAIL_PASS;
    const to = process.env.TL_EMAIL || 'kavinap@uit.ac.in';
    const platform = (process.env.PLATFORM || 'ANDROID').toUpperCase();

    if (!user || !pass) {
        console.warn('EMAIL_USER or EMAIL_PASS not configured. Skipping email.');
        return;
    }

    const transporter = nodemailer.createTransport({
        host: 'smtp.gmail.com',
        port: 465,
        secure: true,
        auth: { user, pass }
    });

    const info = await transporter.sendMail({
        from: `"Seedling Native Automation" <${user}>`,
        to: to,
        subject: `Jenkins Mobile QA [${platform}] - Test Execution Report`,
        html: htmlContent
    });

    console.log('✅ Email sent successfully: %s', info.messageId);
}

sendMail().catch(err => {
    console.error('Failed to send email notification:', err.message);
});
