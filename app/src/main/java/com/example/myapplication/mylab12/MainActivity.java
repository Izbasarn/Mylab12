package com.example.myapplication.mylab12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int VIDEO_CAPTURE_CODE = 1002;
    private static final int GALLERY_REQUEST_CODE = 1003;
    private static final int SIMPLE_VIDEO_REQUEST_CODE = 1004;
    private static final int SHOW_LOCATION_CODE = 1005;

    Button mCaptureMediaBtn;
    Button mCaptureGalleryBtn;
    Button mCaptureVioBtn;
    Button mShowLocationBtn;

    ImageView mImageView;
    Uri imageUri;
    Uri videoUri;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        mCaptureMediaBtn = findViewById(R.id.capture_media_btn);
        mCaptureGalleryBtn = findViewById(R.id.open_gallery_btn);
        mCaptureVioBtn = findViewById(R.id.capture_vio_btn);
        mShowLocationBtn = findViewById(R.id.show_location_btn);

        mCaptureMediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermissionsAndCaptureMedia();
            }
        });

        mCaptureGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mCaptureVioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSimpleVideo();
            }
        });

        mShowLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    showCurrentLocationOnMap();
                }
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Здесь вы можете использовать текущее местоположение (location)
                    Toast.makeText(MainActivity.this, "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            }


        };
    }

    private void checkCameraPermissionsAndCaptureMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                // Request camera and storage permissions
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                // Determine what to do: take a photo or record a video
                showCaptureDialog();
            }
        } else {
            showCaptureDialog();
        }
    }

    private void showCaptureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action")
                .setItems(new CharSequence[]{"Take Photo", "Record Video"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Determine what to do based on the user's choice
                        if (which == 0) {
                            // Take a photo
                            openCamera();
                        } else {
                            // Record a video
                            openVideoCamera();
                        }
                    }
                });

        builder.create().show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    private void openVideoCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, "New Video");
        values.put(MediaStore.Video.Media.DESCRIPTION, "From the video camera");
        videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        startActivityForResult(videoIntent, VIDEO_CAPTURE_CODE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/* video/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void openSimpleVideo() {
        Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        videoIntent.setType("video/*");
        startActivityForResult(videoIntent, SIMPLE_VIDEO_REQUEST_CODE);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Разрешение не предоставлено, запросите его
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, SHOW_LOCATION_CODE);
            return false;
        }
    }

    private void showCurrentLocationOnMap() {
        // Получаем последнее известное местоположение
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Получаем широту и долготу
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Формируем URI для открытия карты с указанными координатами
                            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?z=15");

                            // Создаем интент для открытия карты
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            // Проверяем, есть ли приложение для открытия карты
                            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(mapIntent);
                            } else {
                                Toast.makeText(MainActivity.this, "У вас нет приложения для открытия карты", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Местоположение не найдено", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SHOW_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Разрешение отклонено, выведите сообщение или выполните соответствующие действия
                showLocationPermissionDeniedDialog();
            }
        }
    }

    private void showLocationPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Permission Denied")
                .setMessage("Without location permission, the app cannot show your current location.")
                .setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Перенаправьте пользователя в настройки приложения для предоставления разрешения
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

                @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAPTURE_CODE) {
            // Обработка захвата изображения
            if (resultCode == RESULT_OK) {
                mImageView.setImageURI(imageUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Захват изображения отменен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Не удалось захватить изображение", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == VIDEO_CAPTURE_CODE) {
            // Обработка захвата видео
            if (resultCode == RESULT_OK) {
                // Handle video capture result
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Захват видео отменен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Не удалось захватить видео", Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == GALLERY_REQUEST_CODE || requestCode == SIMPLE_VIDEO_REQUEST_CODE) &&
                resultCode == RESULT_OK && data != null) {
            // Обработка выбора из галереи или простого видео
            Uri selectedUri = data.getData();
            Log.d("Gallery", "Выбранный URI: " + selectedUri);
            // Проверка, является ли выбранный файл изображением или видео
            if (isImageFile(selectedUri)) {
                openImage(selectedUri);
            } else if (isVideoFile(selectedUri)) {
                playVideo(selectedUri);
            } else {
                Toast.makeText(this, "Выбранный файл не является изображением или видео", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to check if the selected file is a video
    private boolean isVideoFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("video/");
    }

    // Method to play the selected video
    private void playVideo(Uri videoUri) {
        Intent playVideoIntent = new Intent(Intent.ACTION_VIEW);
        playVideoIntent.setDataAndType(videoUri, "video/*");
        startActivity(playVideoIntent);
    }
    private boolean isImageFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("image/");
    }

    // Метод для открытия выбранного изображения
    private void openImage(Uri imageUri) {
        Intent openImageIntent = new Intent(Intent.ACTION_VIEW);
        openImageIntent.setDataAndType(imageUri, "image/*");
        startActivity(openImageIntent);
    }}


