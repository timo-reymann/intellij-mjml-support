const {spawn} = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

function runRenderer(input) {
    return new Promise((resolve, reject) => {
        const proc = spawn('node', ["--no-deprecation",path.join(__dirname, 'index.js')]);
        let output = '';
        proc.stdout.on('data', (data) => output += data);
        proc.stderr.on('data', (data) => console.error('stderr:', data.toString()));
        proc.on('error', (err) => reject(err));
        proc.on('close', () => {
            try {
                resolve(JSON.parse(output));
            } catch (e) {
                reject(new Error(`Failed to parse renderer output: ${e.message}\nOutput was: ${output}`));
            }
        });

        proc.stdin.write(input);
        proc.stdin.end();
    });
}

describe('node-mjml-renderer-v5 index.js', () => {
    it('renders valid MJML', async () => {
        const input = JSON.stringify({
            directory: "/tmp",
            content: "<mjml><mj-body><mj-section><mj-column><mj-text>Hello</mj-text></mj-column></mj-section></mj-body></mjml>",
            filePath: "/tmp/test.mjml",
            options: {mjmlConfigPath: ""}
        });

        const result = await runRenderer(input);
        expect(result.html).toContain('<html');
        expect(Array.isArray(result.errors)).toBe(true);
    });

    it('handles invalid MJML', async () => {
        const input = JSON.stringify({
            directory: "/tmp",
            content: "<mj></mj>", // malformed
            filePath: "/tmp/test.mjml",
            options: {mjmlConfigPath: ""}
        });
        const result = await runRenderer(input);
        expect(result.html).toBeNull();
        expect(Array.isArray(result.errors)).toBe(true);
        expect(result.errors.length).toBeGreaterThan(0);
    });

    it('resolves mj-include via includePath', async () => {
        // Use realpath so /tmp symlink (macOS: /tmp -> /private/tmp) doesn't trip MJML 5's
        // includePath security check, which compares fs.realpathSync of both base and include path.
        const dir = fs.realpathSync(fs.mkdtempSync(path.join(os.tmpdir(), 'mjml-v5-include-')));
        const includedPath = path.join(dir, 'header.mjml');
        fs.writeFileSync(includedPath, '<mj-text>included-content</mj-text>');

        const input = JSON.stringify({
            directory: dir,
            content: `<mjml><mj-body><mj-section><mj-column><mj-include path="./header.mjml" /></mj-column></mj-section></mj-body></mjml>`,
            filePath: path.join(dir, 'test.mjml'),
            options: {mjmlConfigPath: ""}
        });

        const result = await runRenderer(input);
        expect(result.html).toContain('included-content');
    });

    it('resolves mj-include from a parent directory via projectRoot', async () => {
        const projectRoot = fs.realpathSync(fs.mkdtempSync(path.join(os.tmpdir(), 'mjml-v5-root-')));
        const sharedDir = path.join(projectRoot, 'shared');
        fs.mkdirSync(sharedDir);
        fs.writeFileSync(path.join(sharedDir, 'header.mjml'), '<mj-text>shared-header-content</mj-text>');

        const templateDir = path.join(projectRoot, 'emails');
        fs.mkdirSync(templateDir);

        const input = JSON.stringify({
            directory: templateDir,
            content: `<mjml><mj-body><mj-section><mj-column><mj-include path="../shared/header.mjml" /></mj-column></mj-section></mj-body></mjml>`,
            filePath: path.join(templateDir, 'welcome.mjml'),
            options: {mjmlConfigPath: ""},
            projectRoot
        });

        const result = await runRenderer(input);
        expect(result.html).toContain('shared-header-content');
    });
});
