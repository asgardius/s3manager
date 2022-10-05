package asgardius.page.s3manager;

import android.content.Context;

import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class CacheDataSourceFactory implements DataSource.Factory {
    private final Context context;
    private final DefaultDataSource.Factory defaultDatasourceFactory;
    private final long maxFileSize;
    SimpleCache simpleCache;
    //DatabaseProvider databaseProvider = ExoDatabaseProvider(this);

    public CacheDataSourceFactory(Context context, SimpleCache simpleCache, long maxFileSize) {
        super();
        this.context = context;
        this.simpleCache = simpleCache;
        //this.maxCacheSize = maxCacheSize;
        this.maxFileSize = maxFileSize;
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
        //DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        defaultDatasourceFactory = new DefaultDataSource.Factory(this.context);
    }

    @Override
    public DataSource createDataSource() {
        //LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
        //simpleCache = new SimpleCache(new File(context.getCacheDir(), "media"), evictor);
        return new CacheDataSource(simpleCache, defaultDatasourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }
}