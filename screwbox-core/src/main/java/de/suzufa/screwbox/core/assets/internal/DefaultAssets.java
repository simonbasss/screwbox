package de.suzufa.screwbox.core.assets.internal;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.suzufa.screwbox.core.Duration;
import de.suzufa.screwbox.core.Time;
import de.suzufa.screwbox.core.assets.AssetLocation;
import de.suzufa.screwbox.core.assets.Assets;
import de.suzufa.screwbox.core.async.Async;
import de.suzufa.screwbox.core.log.Log;
import de.suzufa.screwbox.core.utils.Cache;
import de.suzufa.screwbox.core.utils.Reflections;

public class DefaultAssets implements Assets {

    private final Cache<String, List<AssetLocation>> cache = new Cache<>();

    private final Log log;
    private final Async async;

    private boolean logEnabled = false;

    public DefaultAssets(final Async async, final Log log) {
        this.async = async;
        this.log = log;
    }

    @Override
    public List<AssetLocation> preparePackage(final String packageName) {
        final Time before = Time.now();
        final var loadedAssets = new ArrayList<AssetLocation>();
        final var assets = listAssetLocationsInPackage(packageName);
        for (final var asset : assets) {
            if (!asset.isLoaded()) {
                asset.load();
                loadedAssets.add(asset);
            }
        }
        final var durationMs = Duration.since(before).milliseconds();
        if (logEnabled) {
            log.debug(format("loaded %s assets in %,d ms", loadedAssets.size(), durationMs));
        }

        return loadedAssets;
    }

    @Override
    public List<AssetLocation> listAssetLocationsInPackage(final String packageName) {
        return cache.getOrElse(packageName, () -> fetchAssetInPackage(packageName));
    }

    private List<AssetLocation> fetchAssetInPackage(final String packageName) {
        return Reflections.findClassesInPackage(packageName).stream()
                .flatMap(clazz -> List.of(clazz.getDeclaredFields()).stream())
                .map(AssetLocation::tryToCreateAt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Assets preparePackageAsync(final String packageName) {
        async.run(Assets.class, () -> preparePackage(packageName));
        return this;
    }

    @Override
    public Assets enableLogging() {
        logEnabled = true;
        return this;
    }

    @Override
    public Assets disableLogging() {
        logEnabled = false;
        return this;
    }

}
