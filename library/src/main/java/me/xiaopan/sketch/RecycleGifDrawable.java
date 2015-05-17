/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketch;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import me.xiaopan.sketch.util.CommentUtils;
import pl.droidsonroids.gif.GifDrawable;

public class RecycleGifDrawable extends GifDrawable implements RecycleDrawableInterface {
    private static final String NAME = "RecycleGifDrawable";

    private int cacheRefCount;
    private int displayRefCount;
    private int waitDisplayRefCount;
    private String mimeType;

    public RecycleGifDrawable(AssetFileDescriptor afd) throws IOException {
        super(afd);
    }

    public RecycleGifDrawable(AssetManager assets, String assetName) throws IOException {
        super(assets, assetName);
    }

    public RecycleGifDrawable(ByteBuffer buffer) throws IOException {
        super(buffer);
    }

    public RecycleGifDrawable(byte[] bytes) throws IOException {
        super(bytes);
    }

    public RecycleGifDrawable(FileDescriptor fd) throws IOException {
        super(fd);
    }

    public RecycleGifDrawable(File file) throws IOException {
        super(file);
    }

    public RecycleGifDrawable(String filePath) throws IOException {
        super(filePath);
    }

    public RecycleGifDrawable(Resources res, int id) throws Resources.NotFoundException, IOException {
        super(res, id);
    }

    public RecycleGifDrawable(ContentResolver resolver, Uri uri) throws IOException {
        super(resolver, uri);
    }

    public RecycleGifDrawable(InputStream stream) throws IOException {
        super(stream);
    }

    @Override
    public void setIsDisplayed(String callingStation, boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                displayRefCount++;
            } else {
                if(displayRefCount > 0){
                    displayRefCount--;
                }
            }
        }
        tryRecycle((isDisplayed ? "display" : "hide"), callingStation);
    }

    @Override
    public void setIsCached(String callingStation, boolean isCached) {
        synchronized (this) {
            if (isCached) {
                cacheRefCount++;
            } else {
                if(cacheRefCount > 0){
                    cacheRefCount--;
                }
            }
        }
        tryRecycle((isCached ? "putToCache" : "removedFromCache"), callingStation);
    }

    @Override
    public void setIsWaitDisplay(String callingStation, boolean isWaitDisplay) {
        synchronized (this) {
            if (isWaitDisplay) {
                waitDisplayRefCount++;
            } else {
                if(waitDisplayRefCount > 0){
                    waitDisplayRefCount--;
                }
            }
        }
        tryRecycle((isWaitDisplay ? "waitDisplay" : "displayed"), callingStation);
    }

    @Override
    public int getByteCount() {
        return (int) getAllocationByteCount();
    }

    @Override
    public boolean isRecycled() {
        return super.isRecycled();
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getSize() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return CommentUtils.concat(bitmap.getWidth(), "x", bitmap.getHeight());
        }else{
            return null;
        }
    }

    @Override
    public String getConfig() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null && bitmap.getConfig() != null){
            return bitmap.getConfig().name();
        }else{
            return null;
        }
    }

    @Override
    public String getInfo() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return CommentUtils.concat("RecycleGifDrawable(mimeType=", mimeType, "; hashCode=", Integer.toHexString(bitmap.hashCode()), "; size=", bitmap.getWidth(), "x", bitmap.getHeight(), "; config=", bitmap.getConfig() != null ? bitmap.getConfig().name() : null, "; byteCount=", getByteCount(), ")");
        }else{
            return null;
        }
    }

    private synchronized void tryRecycle(String type, String callingStation) {
        if (cacheRefCount <= 0 && displayRefCount <= 0 && waitDisplayRefCount <= 0 && canRecycle()) {
            if(Sketch.isDebugMode()){
                Log.e(Sketch.TAG, CommentUtils.concat(NAME, " - ", "recycled gif drawable", " - ", callingStation, ":", type, " - ", getInfo()));
            }
            recycle();
        }else{
            if(Sketch.isDebugMode()){
                Log.d(Sketch.TAG, CommentUtils.concat(NAME, " - ", "can't recycle gif drawable", " - ", callingStation, ":", type, " - ", getInfo(), " - ", "references(cacheRefCount=", cacheRefCount, "; displayRefCount=", displayRefCount, "; waitDisplayRefCount=", waitDisplayRefCount, "; canRecycle=", canRecycle(), ")"));
            }
        }
    }

    private boolean canRecycle(){
//        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && !isRecycled();
        return !isRecycled();
    }
}