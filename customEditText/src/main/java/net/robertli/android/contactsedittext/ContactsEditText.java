/*
* Copyright (C) 2012 Robert Li
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package net.robertli.android.contactsedittext;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class ContactsEditText extends MultiAutoCompleteTextView {
    
    private ContactsAdapter mAdapter;
    private Bitmap mLoadingImage;
    private int mDropdownItemHeight;

    public ContactsEditText(Context context) {
        super(context);
        init(context);
    }

    public ContactsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContactsEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // Set image height
        mDropdownItemHeight = 48;
        
        // Set default image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_contact_picture_holo_light, options);
        options.inSampleSize = Utils.calculateInSampleSize(options, mDropdownItemHeight,
                mDropdownItemHeight);
        options.inJustDecodeBounds = false;
        mLoadingImage = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_contact_picture_holo_light, options);

        // Set adapter
        mAdapter = new ContactsAdapter(context);
        setAdapter(mAdapter);

        // Separate entries by commas
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Pop up suggestions after 1 character is typed.
        setThreshold(1);
    }
    
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        return ((Contact) selectedItem).displayName;
    }
    
    /**
     * Holder class to return results to the parent Activity.
     */
    public class Contact {
        public String displayName;
        public Bitmap image;
        public long id;
        public String lookupKey;
    }
    
    private class ContactsAdapter extends CursorAdapter {

        Context mContext;
        LayoutInflater mInflater;

        public ContactsAdapter(Context context) {
            super(context, null, 0);

            mContext = context;
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public Object getItem(int position) {
            Cursor cursor = (Cursor) super.getItem(position);
            Contact contact = new Contact();
            
            String imageUri = cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA_COLUMN);
            Bitmap bitmap = loadContactPhotoThumbnail(imageUri, mDropdownItemHeight);
            if (bitmap == null) {
                bitmap = mLoadingImage;
            }
            
            contact.id = cursor.getLong(ContactsQuery.ID_COLUMN);
            contact.lookupKey = cursor.getString(ContactsQuery.LOOKUP_KEY_COLUMN);
            contact.displayName = cursor.getString(ContactsQuery.DISPLAY_NAME_COLUMN);
            contact.image = bitmap;
            
            return contact;
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (constraint == null || constraint.length() == 0) {
                return mContext.getContentResolver().query(
                        ContactsQuery.CONTENT_URI,
                        ContactsQuery.PROJECTION,
                        ContactsQuery.SELECTION,
                        null,
                        ContactsQuery.SORT_ORDER); 
            }

            return mContext.getContentResolver().query(
                    Uri.withAppendedPath(ContactsQuery.FILTER_URI, constraint.toString()),
                    ContactsQuery.PROJECTION,
                    ContactsQuery.SELECTION,
                    null,
                    ContactsQuery.SORT_ORDER);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            final View dropdownView = mInflater.inflate(R.layout.contacts_dropdown_item,
                    viewGroup, false);

            ViewHolder holder = new ViewHolder();
            holder.text = (TextView) dropdownView.findViewById(android.R.id.text1); 
            holder.image = (ImageView) dropdownView.findViewById(android.R.id.icon);

            dropdownView.setTag(holder);

            return dropdownView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();

            final String displayName = cursor.getString(ContactsQuery.DISPLAY_NAME_COLUMN);
            final String imageUri = cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA_COLUMN);

            holder.text.setText(displayName);

            Bitmap bitmap = loadContactPhotoThumbnail(imageUri, mDropdownItemHeight);
            if (bitmap == null) {
                bitmap = mLoadingImage;
            }
            holder.image.setImageBitmap(bitmap);
        }

        private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {
            AssetFileDescriptor afd = null;

            try {
                Uri thumbUri;
                if (Utils.hasHoneycomb() && photoData != null) {
                    thumbUri = Uri.parse(photoData);
                } else {
                    final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);
                    thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
                }

                afd = mContext.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
                FileDescriptor fd = afd.getFileDescriptor();

                if (fd != null) {
                    return Utils.decodeSampledBitmapFromDescriptor(fd, imageSize, imageSize);
                }
            } catch (FileNotFoundException e) {
            } finally {
                if (afd != null) {
                    try {
                        afd.close();
                    } catch (IOException e) {} 
                }
            }

            return null;
        }

    }

    /**
     * Class to hold the dropdown item's views. Used as a tag to bind the child views to its
     * parent.
     */
    private class ViewHolder {
        public TextView text;
        public ImageView image;
    }

    /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    private static interface ContactsQuery {

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = Contacts.CONTENT_URI;

        // The search/filter query Uri
        final static Uri FILTER_URI = Contacts.CONTENT_FILTER_URI;

        // The selection clause for the CursorLoader query. The search criteria defined here
        // restrict results to contacts that have a display name, are linked to visible groups,
        // and have a phone number.  Notice that the search on the string provided by the user
        // is implemented by appending the search string to CONTENT_FILTER_URI.
        @SuppressLint("InlinedApi")
        final static String SELECTION =
                (Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME) +
                "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1 AND " +
                Contacts.HAS_PHONE_NUMBER + "=1";

        // The desired sort order for the returned Cursor. Not sure what apps like Mms use, but
        // TIMES_CONTACTED seems to be fairly useful for this purpose.
        final static String SORT_ORDER = Contacts.TIMES_CONTACTED + " DESC";

        // The projection for the CursorLoader query. This is a list of columns that the Contacts
        // Provider should return in the Cursor.
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {

            // The contact's row id
            Contacts._ID,

            // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
            // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
            // a "permanent" contact URI.
            Contacts.LOOKUP_KEY,

            // In platform version 3.0 and later, the Contacts table contains
            // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
            // some other useful identifier such as an email address. This column isn't
            // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
            // instead.
            Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,

            // In Android 3.0 and later, the thumbnail image is pointed to by
            // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
            // you generate the pointer from the contact's ID value and constants defined in
            // android.provider.ContactsContract.Contacts.
            Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID
        };
        
        // The query column numbers which map to each value in the projection
        final static int ID_COLUMN = 0;
        final static int LOOKUP_KEY_COLUMN = 1;
        final static int DISPLAY_NAME_COLUMN = 2;
        final static int PHOTO_THUMBNAIL_DATA_COLUMN = 3;
    }

    private static class Utils {

        // Prevents instantiation.
        private Utils() {}

        /**
         * Uses static final constants to detect if the device's platform version is Honeycomb or
         * later.
         */
        public static boolean hasHoneycomb() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        }
        
        public static Bitmap decodeSampledBitmapFromDescriptor(
                FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        }

        public static int calculateInSampleSize(BitmapFactory.Options options,
                int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
                // with both dimensions larger than or equal to the requested height and width.
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

                // This offers some additional logic in case the image has a strange
                // aspect ratio. For example, a panorama may have a much larger
                // width than height. In these cases the total pixels might still
                // end up being too large to fit comfortably in memory, so we should
                // be more aggressive with sample down the image (=larger inSampleSize).

                final float totalPixels = width * height;

                // Anything more than 2x the requested pixels we'll sample down further
                final float totalReqPixelsCap = reqWidth * reqHeight * 2;

                while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++;
                }
            }
            return inSampleSize;
        }

    }

}
