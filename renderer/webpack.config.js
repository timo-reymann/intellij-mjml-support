const nodeExternals = require("webpack-node-externals")
module.exports = {
    mode: 'production',
    node: {
        global: false
    },
    entry: [
        './index.js'
    ],
    output: {
        filename: 'render.js',
        libraryTarget: "commonjs2"
    },
    resolve: {
        modules: [
            'node_modules'
        ]
    },
    target: 'node',
    externals: [
        nodeExternals({
            allowlist: [
                /\w{3,}.+/,
                'he'
            ]
        })
    ]
}
