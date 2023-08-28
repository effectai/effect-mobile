const {getDefaultConfig, mergeConfig} = require('@react-native/metro-config');

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
    resolver: {
        nodeModulesPaths: ['/home/jesse/repos/effectai/effect-js'],
    }
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
