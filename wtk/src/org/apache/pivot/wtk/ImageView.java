/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ImageUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.media.Image;

/**
 * Component that displays an image.
 */
@DefaultProperty("image")
public class ImageView extends Component {
    /**
     * Translates between image and context data during data binding.
     */
    public interface ImageBindMapping {
        /**
         * Defines the supported load type mappings.
         */
        public enum Type {
            IMAGE,
            URL,
            NAME
        }

        /**
         * @return The load type supported by this mapping.
         */
        public Type getType();

        /**
         * Converts a value from the bind context to an image representation
         * during a {@link Component#load(Object)} operation.
         *
         * @param value The value returned from the bound object.
         * @return The image converted from the bound value.
         */
        public Image toImage(Object value);

        /**
         * Converts a value from the bind context to an image location during a
         * {@link Component#load(Object)} operation.
         *
         * @param value The value returned from the bound object.
         * @return The value converted to an image URL.
         */
        public URL toImageURL(Object value);

        /**
         * Converts a value from the bind context to an image resource name
         * during a {@link Component#load(Object)} operation.
         *
         * @param value The value returned from the bound object.
         * @return The value converted to an image name.
         */
        public String toImageName(Object value);

        /**
         * Converts an image to a value to be stored in the bind context
         * during a {@link Component#store(Object)} operation.
         * <p> Note: if the bind type is {@link Type#URL} or {@link Type#NAME} then
         * this will likely entail also persisting the image itself
         * somewhere else and returning the name/location of the stored
         * image.
         *
         * @param image The image currently stored in the image view.
         * @return The image converted to a value suitable for persistence
         * in the bound object.
         */
        public Object valueOf(Image image);
    }

    private Image image = null;
    private boolean asynchronous = false;
    private String imageKey = null;
    private BindType imageBindType = BindType.BOTH;
    private ImageBindMapping imageBindMapping = null;

    private ImageViewListener.Listeners imageViewListeners = new ImageViewListener.Listeners();
    private ImageViewBindingListener.Listeners imageViewBindingListeners = new ImageViewBindingListener.Listeners();

    // Maintains a mapping of image URL to image views that should be notified when
    // an asynchronously loaded image is available
    private static HashMap<java.net.URI, ArrayList<ImageView>> loadMap = new HashMap<>();

    /**
     * Creates an empty image view.
     */
    public ImageView() {
        this(null);
    }

    /**
     * Creates an image view with the given image.
     *
     * @param image The initial image to set, or {@code null} for no image.
     */
    public ImageView(final Image image) {
        setImage(image);

        installSkin(ImageView.class);
    }

    /**
     * Returns the image view's current image.
     *
     * @return The current image, or {@code null} if no image is set.
     */
    public final Image getImage() {
        return image;
    }

    /**
     * Sets the image view's current image.
     *
     * @param image The image to set, or {@code null} for no image.
     */
    public final void setImage(final Image image) {
        Image previousImage = this.image;

        if (previousImage != image) {
            this.image = image;
            imageViewListeners.imageChanged(this, previousImage);
        }
    }

    /**
     * Sets the image view's current image by URL. <p> If the icon already
     * exists in the application context resource cache, the cached value will
     * be used. Otherwise, the icon will be loaded synchronously and added to
     * the cache.
     *
     * @param imageURL The location of the image to set.
     */
    public final void setImage(final URL imageURL) {
        Utils.checkNull(imageURL, "imageURL");

        Image imageLocal = (Image) ApplicationContext.getResourceCache().get(imageURL);

        if (imageLocal == null) {
            // Convert to URI because using a URL as a key causes performance problems
            final java.net.URI imageURI;
            try {
                imageURI = imageURL.toURI();
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }

            if (asynchronous) {
                if (loadMap.containsKey(imageURI)) {
                    // Add this to the list of image views that are interested in
                    // the image at this URL
                    loadMap.get(imageURI).add(this);
                } else {
                    Image.load(imageURL, new TaskAdapter<>(new TaskListener<Image>() {
                        @Override
                        public void taskExecuted(final Task<Image> task) {
                            Image imageLoadedLocal = task.getResult();

                            // Update the contents of all image views that requested this image
                            for (ImageView imageView : loadMap.get(imageURI)) {
                                imageView.setImage(imageLoadedLocal);
                            }

                            loadMap.remove(imageURI);

                            // Add the image to the cache
                            ApplicationContext.getResourceCache().put(imageURL, imageLoadedLocal);
                        }

                        @Override
                        public void executeFailed(final Task<Image> task) {
                            // No-op
                        }
                    }));

                    loadMap.put(imageURI, new ArrayList<>(this));
                }
            } else {
                imageLocal = Image.loadFromCache(imageURL);
            }
        }

        setImage(imageLocal);
    }

    /**
     * Sets the image view's image by
     * {@linkplain ClassLoader#getResource(String) resource name}.
     *
     * @param imageName The resource name of the image to set.
     * @see #setImage(URL)
     * @see ImageUtils#findByName(String,String)
     */
    public final void setImage(final String imageName) {
        setImage(ImageUtils.findByName(imageName, "image"));
    }

    /**
     * Returns the image view's asynchronous flag.
     *
     * @return {@code true} if images specified via URL will be loaded in the
     * background; {@code false} if they will be loaded synchronously.
     */
    public boolean isAsynchronous() {
        return asynchronous;
    }

    /**
     * Sets the image view's asynchronous flag.
     *
     * @param asynchronous {@code true} if images specified via URL will be
     * loaded in the background; {@code false} if they will be loaded
     * synchronously.
     */
    public void setAsynchronous(final boolean asynchronous) {
        if (this.asynchronous != asynchronous) {
            this.asynchronous = asynchronous;
            imageViewListeners.asynchronousChanged(this);
        }
    }

    /**
     * Returns the image view's image key.
     *
     * @return The image key, or {@code null} if no key is set.
     */
    public String getImageKey() {
        return imageKey;
    }

    /**
     * Sets the image view's image key.
     *
     * @param imageKey The image key, or {@code null} to clear the binding.
     */
    public void setImageKey(final String imageKey) {
        String previousImageKey = this.imageKey;

        if (previousImageKey != imageKey) {
            this.imageKey = imageKey;
            imageViewBindingListeners.imageKeyChanged(this, previousImageKey);
        }
    }

    public BindType getImageBindType() {
        return imageBindType;
    }

    public void setImageBindType(final BindType imageBindType) {
        Utils.checkNull(imageBindType, "imageBindType");

        BindType previousImageBindType = this.imageBindType;

        if (previousImageBindType != imageBindType) {
            this.imageBindType = imageBindType;
            imageViewBindingListeners.imageBindTypeChanged(this, previousImageBindType);
        }
    }

    public ImageBindMapping getImageBindMapping() {
        return imageBindMapping;
    }

    public void setImageBindMapping(final ImageBindMapping imageBindMapping) {
        ImageBindMapping previousImageBindMapping = this.imageBindMapping;

        if (previousImageBindMapping != imageBindMapping) {
            this.imageBindMapping = imageBindMapping;
            imageViewBindingListeners.imageBindMappingChanged(this, previousImageBindMapping);
        }
    }

    @Override
    public void load(final Object context) {
        if (imageKey != null && JSON.containsKey(context, imageKey)
            && imageBindType != BindType.STORE) {
            Object value = JSON.get(context, imageKey);

            if (imageBindMapping != null) {
                switch (imageBindMapping.getType()) {
                    case IMAGE:
                        value = imageBindMapping.toImage(value);
                        break;

                    case URL:
                        value = imageBindMapping.toImageURL(value);
                        break;

                    case NAME:
                        value = imageBindMapping.toImageName(value);
                        break;

                    default:
                        break;
                }
            }

            if (value == null || value instanceof Image) {
                setImage((Image) value);
            } else if (value instanceof URL) {
                setImage((URL) value);
            } else if (value instanceof String) {
                setImage((String) value);
            } else {
                throw new IllegalArgumentException(getClass().getName() + " can't bind to " + value + ".");
            }
        }
    }

    @Override
    public void store(final Object context) {
        if (imageKey != null && imageBindType != BindType.LOAD) {
            JSON.put(context, imageKey,
                (imageBindMapping == null) ? image : imageBindMapping.valueOf(image));
        }
    }

    @Override
    public void clear() {
        if (imageKey != null) {
            clearImage();
        }
    }

    /**
     * @return The image view listener list.
     */
    public ListenerList<ImageViewListener> getImageViewListeners() {
        return imageViewListeners;
    }

    /**
     * @return The image view binding listener list.
     */
    public ListenerList<ImageViewBindingListener> getImageViewBindingListeners() {
        return imageViewBindingListeners;
    }

    /**
     * Force a reset of the image (and its listeners).
     */
    public void clearImage() {
        setImage((Image) null);
    }

}
