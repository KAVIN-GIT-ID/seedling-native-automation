const fs = require('fs');
const path = require('path');

// Extract arguments / environment variables
const envName = (process.env.ENVIRONMENT || process.env.ENV || 'QA').toUpperCase();
const platform = (process.env.PLATFORM || 'ANDROID').toUpperCase();
const component = process.env.COMPONENT || 'All Tests';
const reportUrl = process.env.REPORT_URL || '#';
const runUrl = process.env.RUN_URL || '#';
const jobStatus = (process.env.JOB_STATUS || 'success').toLowerCase();
const branchName = process.env.BRANCH_NAME || process.env.GITHUB_REF_NAME || 'main';

// Path to TestNG XML report
const reportXmlPath = path.join(process.cwd(), 'target', 'surefire-reports', 'testng-results.xml');

let total = 0;
let passed = 0;
let failed = 0;
let skipped = 0;
let duration = 'N/A';
let failedTestsList = [];
let passedTestsList = [];

if (fs.existsSync(reportXmlPath)) {
    try {
        const xmlData = fs.readFileSync(reportXmlPath, 'utf8');

        // Extract stats safely from the <testng-results> tag
        const passedMatch = xmlData.match(/passed="(\d+)"/);
        const failedMatch = xmlData.match(/failed="(\d+)"/);
        const skippedMatch = xmlData.match(/skipped="(\d+)"/);
        const totalMatch = xmlData.match(/total="(\d+)"/);
        const durationMatch = xmlData.match(/duration-ms="(\d+)"/);

        passed = passedMatch ? parseInt(passedMatch[1]) : 0;
        failed = failedMatch ? parseInt(failedMatch[1]) : 0;
        skipped = skippedMatch ? parseInt(skippedMatch[1]) : 0;
        total = totalMatch ? parseInt(totalMatch[1]) : (passed + failed + skipped);

        if (durationMatch) {
            const ms = parseInt(durationMatch[1]);
            const secs = Math.floor(ms / 1000);
            const mins = Math.floor(secs / 60);
            duration = mins > 0 ? `${mins}m ${secs % 60}s` : `${secs}s`;
        }

        // Extract failed test method details
        const testMethodRegex = /<test-method[^>]*name="([^"]+)"[^>]*status="FAIL"[^>]*>([\s\S]*?)<\/test-method>/g;
        let match;
        while ((match = testMethodRegex.exec(xmlData)) !== null) {
            const testName = match[1];
            const content = match[2];
            const exceptionMatch = content.match(/<exception class="([^"]+)">[\s\S]*?<message>[\s\S]*?<!\[CDATA\[([\s\S]*?)\]\]>[\s\S]*?<\/exception>/);
            let errorMsg = 'Assertion failure or execution exception';
            if (exceptionMatch && exceptionMatch[2]) {
                errorMsg = exceptionMatch[2].trim().split('\n')[0];
            } else {
                const simpleMsg = content.match(/<message>([\s\S]*?)<\/message>/);
                if (simpleMsg && simpleMsg[1]) {
                    errorMsg = simpleMsg[1].replace(/<!\[CDATA\[|\]\]>/g, '').trim().split('\n')[0];
                }
            }
            failedTestsList.push({ name: testName, error: errorMsg });
        }

        // Extract passed test names
        const passMethodRegex = /<test-method[^>]*name="([^"]+)"[^>]*status="PASS"[^>]*is-config="false"/g;
        let passMatch;
        while ((passMatch = passMethodRegex.exec(xmlData)) !== null) {
            passedTestsList.push(passMatch[1]);
        }
    } catch (e) {
        console.warn('Notice: Error parsing testng-results.xml:', e.message);
    }
}

// Fallback status determination if XML wasn't present or job status failed
const isSuccess = jobStatus === 'success' && failed === 0;
const statusBadgeHtml = isSuccess 
    ? `<span class="status-badge status-pass">✓ PASSED</span>`
    : `<span class="status-badge status-fail">✕ FAILED</span>`;

const now = new Date();
const dateOptions = { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true, timeZoneName: 'short' };
const currentDateStr = now.toLocaleDateString('en-GB', dateOptions);

const htmlTemplate = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Native App Test Report</title>
  <style>
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      background-color: #F4F6F5;
      margin: 0;
      padding: 24px 12px;
      color: #263229;
    }
    .email-container {
      max-width: 620px;
      margin: 0 auto;
      background-color: #FFFFFF;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
      border: 1px solid #E4E9E5;
    }
    .header {
      background-color: #FFFFFF;
      padding: 28px 28px 22px 28px;
      text-align: center;
      border-bottom: 1px solid #EDEFEC;
    }
    .header-title {
      font-size: 19px;
      font-weight: 700;
      margin: 0 0 14px 0;
      color: #1A1A1A;
    }
    .status-badge {
      display: inline-block;
      padding: 6px 18px;
      border-radius: 9999px;
      font-size: 13px;
      font-weight: 700;
      letter-spacing: 0.5px;
    }
    .status-fail {
      background-color: #FDECEA;
      color: #C62828;
    }
    .status-pass {
      background-color: #E8F5E9;
      color: #2E7D32;
    }
    .native-tag {
      display: inline-block;
      padding: 6px 14px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 700;
      background-color: #EDE9FE;
      color: #6D28D9;
      letter-spacing: 0.5px;
      margin-left: 6px;
    }
    .platform-tag {
      display: inline-block;
      padding: 6px 14px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 700;
      background-color: #F3F4F6;
      color: #374151;
      letter-spacing: 0.5px;
      margin-left: 6px;
    }
    .body-content {
      padding: 28px;
    }
    .intro-text {
      font-size: 15px;
      color: #263229;
      margin: 0 0 22px 0;
      line-height: 1.6;
    }
    .metrics-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 22px;
    }
    .metrics-table td {
      width: 25%;
      text-align: center;
      background-color: #FAFAFA;
      border: 1px solid #EDEFEC;
      padding: 14px 6px;
    }
    .metric-value {
      font-size: 21px;
      font-weight: 700;
      margin-bottom: 2px;
    }
    .metric-label {
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #6B7280;
      font-weight: 600;
    }
    .meta-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 22px;
      background-color: #FAFAFA;
      border-radius: 10px;
      overflow: hidden;
      border: 1px solid #EDEFEC;
    }
    .meta-table td {
      padding: 13px 18px;
      font-size: 14px;
      border-bottom: 1px solid #EDEFEC;
    }
    .meta-table tr:last-child td {
      border-bottom: none;
    }
    .meta-label {
      font-weight: 600;
      color: #6B7280;
      width: 35%;
    }
    .meta-val {
      font-weight: 600;
      color: #1A1A1A;
      word-break: break-all;
    }
    .btn-container {
      margin: 26px 0 6px 0;
      text-align: center;
    }
    .btn-primary {
      display: inline-block;
      background-color: #1A1A1A;
      color: #FFFFFF !important;
      text-decoration: none;
      font-weight: 700;
      font-size: 15px;
      padding: 13px 30px;
      border-radius: 8px;
      margin-bottom: 12px;
    }
    .btn-secondary {
      display: inline-block;
      color: #4B5563 !important;
      text-decoration: none;
      font-weight: 600;
      font-size: 13px;
      padding: 8px 16px;
      border: 1px solid #D1D5DB;
      border-radius: 8px;
      background-color: #FFFFFF;
    }
    .card-failures {
      background-color: #FDECEA;
      border-left: 4px solid #C62828;
      border-radius: 8px;
      padding: 18px 20px;
      margin-bottom: 22px;
      font-size: 13px;
      color: #9B3226;
      line-height: 1.7;
    }
    .card-title {
      font-weight: 700;
      font-size: 13px;
      margin-bottom: 10px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #1A1A1A;
    }
    .failure-item {
      margin-bottom: 10px;
      padding-bottom: 10px;
      border-bottom: 1px dashed #F5B5AE;
    }
    .failure-item:last-child {
      border-bottom: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }
    .failure-name {
      font-weight: 700;
      color: #9B3226;
    }
    .failure-err {
      font-family: monospace;
      font-size: 12px;
      color: #374151;
      background: #FFFFFF;
      padding: 6px 10px;
      border-radius: 4px;
      margin-top: 4px;
      white-space: pre-wrap;
      word-break: break-all;
      display: block;
    }
    .footer {
      background-color: #FAFAFA;
      padding: 20px 28px;
      text-align: center;
      font-size: 12px;
      color: #9CA3AF;
      border-top: 1px solid #EDEFEC;
    }
  </style>
</head>
<body>
  <div class="email-container">
    <div class="header">
      <table role="presentation" align="center" cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto 18px auto;">
        <tr>
          <td style="vertical-align: middle; padding-right: 8px;">
            <img src="https://cdn.jsdelivr.net/gh/gilbarbara/logos/logos/appium.svg" width="34" height="34" alt="Appium" style="display: block; height: 34px; width: 34px; border: 0;" />
          </td>
          <td style="vertical-align: middle;">
            <span style="font-size: 17px; font-weight: 700; color: #1A1A1A; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">Seedling Native Automation Suite</span>
          </td>
        </tr>
      </table>
      <div class="header-title">Appium Test Run Report</div>
      ${statusBadgeHtml}
      <span class="native-tag">NATIVE</span>
      <span class="platform-tag">${platform}</span>
    </div>

    <div class="body-content">
      <p class="intro-text">An Appium native automation run has completed for <strong>${component}</strong> on <strong>${envName}</strong> (${platform}). Details of the run are below.</p>

      <table class="metrics-table">
        <tr>
          <td>
            <div class="metric-value" style="color: #1A1A1A;">${total}</div>
            <div class="metric-label">Total</div>
          </td>
          <td>
            <div class="metric-value" style="color: #2E7D32;">${passed}</div>
            <div class="metric-label">Passed</div>
          </td>
          <td>
            <div class="metric-value" style="color: #C62828;">${failed}</div>
            <div class="metric-label">Failed</div>
          </td>
          <td>
            <div class="metric-value" style="color: #6B7280;">${skipped}</div>
            <div class="metric-label">Skipped</div>
          </td>
        </tr>
      </table>

      <table class="meta-table">
        <tr>
          <td class="meta-label">Environment</td>
          <td class="meta-val">${envName}</td>
        </tr>
        <tr>
          <td class="meta-label">Mobile Platform</td>
          <td class="meta-val">${platform}</td>
        </tr>
        <tr>
          <td class="meta-label">Component / Suite</td>
          <td class="meta-val">${component}</td>
        </tr>
        <tr>
          <td class="meta-label">Execution Duration</td>
          <td class="meta-val">${duration}</td>
        </tr>
        <tr>
          <td class="meta-label">Git Branch</td>
          <td class="meta-val">${branchName}</td>
        </tr>
        <tr>
          <td class="meta-label">Execution Time</td>
          <td class="meta-val">${currentDateStr}</td>
        </tr>
      </table>

      ${failedTestsList.length > 0 ? `
      <div class="card-failures">
        <div class="card-title">❌ Failed Tests Breakdown (${failedTestsList.length})</div>
        ${failedTestsList.map(item => `
          <div class="failure-item">
            <div class="failure-name">✘ ${item.name}</div>
            <div class="failure-err">${item.error}</div>
          </div>
        `).join('')}
      </div>
      ` : ''}

      <div class="btn-container">
        <div>
          <a href="${reportUrl}" target="_blank" class="btn-primary">Open Full Report &amp; Screen Recording</a>
        </div>
        ${runUrl && runUrl !== '#' ? `
        <div style="margin-top: 10px;">
          <a href="${runUrl}" target="_blank" class="btn-secondary">View GitHub Actions Run Log</a>
        </div>
        ` : ''}
      </div>
    </div>

    <div class="footer">
      This is an automated notification from Seedling Native QA Automation &bull; Appium 2.x Framework<br>
      © 2026 Seedling Social. All rights reserved.
    </div>
  </div>
</body>
</html>`;

const outputPath = path.join(process.cwd(), 'email-body.html');
fs.writeFileSync(outputPath, htmlTemplate, 'utf8');
console.log('✅ Generated dynamic HTML email report at:', outputPath);
