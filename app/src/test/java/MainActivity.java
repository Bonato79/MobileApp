import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Camera camera;
    private SurfaceView preview;
    private SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar e solicitar permissão de câmera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                // Permissão de câmera não concedida, trate esse caso adequadamente
            }
        }
    }

    private void startCamera() {
        camera = Camera.open();

        preview = new SurfaceView(this);
        holder = preview.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (holder.getSurface() == null) {
                    return;
                }

                try {
                    camera.stopPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = getBestPreviewSize(width, height, parameters);

                    if (size != null) {
                        parameters.setPreviewSize(size.width, size.height);
                        camera.setParameters(parameters);
                        camera.setDisplayOrientation(90);
                        camera.startPreview();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });

        FrameLayout frameLayout = findViewById(R.id.camera_preview);
        frameLayout.addView(preview);
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (bestSize == null) {
                    bestSize = size;
                } else {
                    int bestArea = bestSize.width * bestSize.height;
                    int newArea = size.width * size.height;

                    if (newArea > bestArea) {
                        bestSize = size;
                    }
                }
            }
        }
        return bestSize;
    }
}
