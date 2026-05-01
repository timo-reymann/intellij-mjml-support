const {spawn} = require('child_process');
const path = require('path');

function runRenderer(input) {
    return new Promise((resolve, reject) => {
        const proc = spawn('node', ["--no-deprecation",path.join(__dirname, 'index.js')]);
        let output = '';
        proc.stdout.on('data', (data) => output += data);
        proc.stderr.on('data', (data) => reject(data.toString()));
        proc.on('close', () => {
            resolve(JSON.parse(output))
        });
        proc.stderr.on('data', (data) => {
            console.error('stderr:', data.toString());
            reject(data.toString());
        });

        proc.stdin.write(input);
        proc.stdin.end();
    });
}

describe('node-mjml-renderer index.js', () => {
    it('renders valid MJML', async () => {
        const input = JSON.stringify({
            directory: "/tmp",
            content: "<mjml><mj-body><mj-section><mj-column><mj-text>Hello</mj-text></mj-column></mj-section></mj-body></mjml>",
            filePath: "/tmp/test.mjml",
            options: {mjmlConfigPath: ""}
        });

        console.log("PROC")
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

});
