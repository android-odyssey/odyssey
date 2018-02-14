/*
 * Copyright (C) 2018 Team Gateship-One
 * (Hendrik Borghorst & Frederik Luetkes)
 *
 * The AUTHORS.md file contains a detailed contributors list:
 * <https://github.com/gateship-one/odyssey/blob/master/AUTHORS.md>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gateshipone.odyssey.artworkdatabase;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import org.gateshipone.odyssey.models.AlbumModel;
import org.gateshipone.odyssey.models.ArtistModel;

import java.util.Map;

/**
 * Simple LRU-based caching for album & artist images. This could reduce CPU usage
 * for the cost of memory usage by caching decoded {@link Bitmap} objects in a {@link LruCache}.
 */
public class BitmapCache {
    private static final String TAG = BitmapCache.class.getSimpleName();

    private static final int mMaxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    /**
     * Maximum size of the cache in kilobytes
     */
    private static final int mCacheSize = mMaxMemory / 4;

    /**
     * Hash prefix for album images
     */
    private static final String ALBUM_PREFIX = "A_";

    /**
     * Hash prefix for artist images
     */
    private static final String ARTIST_PREFIX = "B_";

    /**
     * Private cache instance
     */
    private LruCache<String, Bitmap> mCache;

    /**
     * Singleton instance
     */
    private static BitmapCache mInstance;

    private BitmapCache() {
        mCache = new LruCache<String, Bitmap>(mCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }

        };
    }

    public static synchronized BitmapCache getInstance() {
        if (mInstance == null) {
            mInstance = new BitmapCache();
        }
        return mInstance;
    }

    /**
     * Tries to get an album image from the cache
     *
     * @param album Album object to try
     * @return Bitmap if cache hit, null otherwise
     */
    public synchronized Bitmap requestAlbumBitmap(AlbumModel album) {
        return mCache.get(getAlbumHash(album));
    }

    /**
     * Puts an album image to the cache
     *
     * @param album Album object to use for cache key
     * @param bm    Bitmap to store in cache
     */
    public synchronized void putAlbumBitmap(AlbumModel album, Bitmap bm) {
        if (bm != null) {
            mCache.put(getAlbumHash(album), bm);
        }
    }

    /**
     * Removes an album image from the cache
     *
     * @param album Album object to use for cache key
     */
    public synchronized void removeAlbumBitmap(AlbumModel album) {
        mCache.remove(getAlbumHash(album));
    }

    /**
     * Private hash method for cache key
     *
     * @param album Album to calculate the key from
     * @return Hash string for cache key
     */
    private String getAlbumHash(AlbumModel album) {
        String hashString = ALBUM_PREFIX;
        final long albumID = album.getAlbumID();

        // Use albumID as key if available
        if (albumID != -1) {
            hashString += String.valueOf(albumID);
            return hashString;
        }

        // Else use artist and album name
        final String albumArtist = album.getArtistName();
        final String albumName = album.getAlbumName();

        hashString += albumArtist + '_' + albumName;
        return hashString;
    }

    /*
     * Begin of artist image handling
     */

    /**
     * Tries to get an artist image from the cache
     *
     * @param artist Artist object to check in cache
     * @return Bitmap if cache hit, null otherwise
     */
    public synchronized Bitmap requestArtistImage(ArtistModel artist) {
        return mCache.get(getArtistHash(artist));
    }

    /**
     * Puts an artist image to the cache
     *
     * @param artist Artist object used as cache key
     * @param bm     Bitmap to store in cache
     */
    public synchronized void putArtistImage(ArtistModel artist, Bitmap bm) {
        if (bm != null) {
            mCache.put(getArtistHash(artist), bm);
        }
    }

    /**
     * Removes an artist image from the cache
     *
     * @param artist Artist object used as cache key
     */
    public synchronized void removeArtistImage(ArtistModel artist) {
        mCache.remove(getArtistHash(artist));
    }

    /**
     * Private hash method for cache key
     *
     * @param artist Artist used as cache key
     * @return Hash string for cache key
     */
    private String getArtistHash(ArtistModel artist) {
        String hashString = ARTIST_PREFIX;

        final long artistID = artist.getArtistID();

        // Use albumID as key if available
        if (artistID != -1) {
            hashString += String.valueOf(artistID);
            return hashString;
        }

        hashString += artist.getArtistName();

        return hashString;
    }

    /**
     * Debug method to provide performance evaluation metrics
     */
    private void printUsage() {
        Log.v(TAG, "Cache usage: " + ((mCache.size() * 100) / mCache.maxSize()) + '%');
        int missCount = mCache.missCount();
        int hitCount = mCache.hitCount();
        if (missCount > 0) {
            Log.v(TAG, "Cache hit count: " + hitCount + " miss count: " + missCount + " Miss rate: " + ((hitCount * 100) / missCount) + '%');
        }
        Log.v(TAG, "Memory usage: " + (getMemoryUsage() / (1024 * 1024)) + " MB");
    }

    private long getMemoryUsage() {
        Map<String, Bitmap> snapShot = mCache.snapshot();
        long bytes = 0;
        for (Bitmap bitmap : snapShot.values()) {
            bytes += bitmap.getAllocationByteCount();
        }

        return bytes;
    }
}
