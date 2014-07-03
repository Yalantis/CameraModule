CameraModule
============
Camera module for Android applications.

Screenshots:
https://www.dropbox.com/sh/2d7svoykpwpwmbw/AAAveLqvtaJ2Zt5NHaEu3-QSa

Usage
--------
In your Application class call managers initializer:

```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ManagerInitializer.i.init(getApplicationContext());
    }
}
```

Then call `CameraActivity` to use camera:

```java
Intent intent = new Intent(this, CameraActivity.class);
intent.putExtra(CameraActivity.PATH, Environment.getExternalStorageDirectory().getPath());
intent.putExtra(CameraActivity.OPEN_PHOTO_PREVIEW, true);
startActivity(intent);
```

Customising
--------
To create custom layout for `CameraFragment`, please use this ids:

`camera_preview` - Container for `CameraPreview` that extends ViewGroup.

`capture` - View for capturing photos

`zoom_ratio` - `TextView` for displaying zoom ratio value

`flash_mode` - `ImageButton` for displaying and switching flash mode

`progress` - `ProgressBar` that indicates that capturing or saving photo in progress

`camera_settings` - `ImageButton` that call `CameraSettingsDialogFragment`

Then put layout resources id to intent extras for `CameraActivty`:
```java
Intent intent = new Intent(this, CameraActivity.class);
intent.putExtra(CameraActivity.PATH, Environment.getExternalStorageDirectory().getPath());
intent.putExtra(CameraActivity.OPEN_PHOTO_PREVIEW, true);
intent.putExtra(CameraActivity.LAYOUT_ID, R.layout.fragment_camera_custom);
startActivity(intent);
```

License
--------

	The MIT License (MIT)
	
		Copyright (c) 2014 Zillow
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is furnished
	to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
