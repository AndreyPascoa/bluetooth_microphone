package com.example.bluetoothjava;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;
    private static final String TARGET_DEVICE_NAME = "F9-5C";  // Nome do seu fone de ouvido Bluetooth

    private AudioManager audioManager;
    private BluetoothAdapter bluetoothAdapter;
    private MediaRecorder mediaRecorder;
    private OutputStream outputStream;

    private Button startRecordingButton;
    private Button stopRecordingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRecordingButton = findViewById(R.id.startRecordingButton);
        stopRecordingButton = findViewById(R.id.stopRecordingButton);

        Log.d("BluetoothSCO", "onCreate chamado. Inicializando AudioManager e BluetoothAdapter.");

        // Inicialize o AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Inicialize o BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verifique e solicite permissões se necessário
        if (checkAndRequestPermissions()) {
            Log.d("BluetoothSCO", "Permissões concedidas. Iniciando o processo Bluetooth.");
            startBluetoothProcess();
        } else {
            Log.d("BluetoothSCO", "Permissões não concedidas. Processo Bluetooth abortado.");
        }

        // Configurar botões de gravação
        startRecordingButton.setOnClickListener(v -> startRecording());
        stopRecordingButton.setOnClickListener(v -> stopRecording());
    }

    private boolean checkAndRequestPermissions() {
        boolean hasAllPermissions = true;

        // Verifique se a permissão de Bluetooth já foi concedida (API 31+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            hasAllPermissions = false;
        }

        // Verifique se a permissão de gravação de áudio já foi concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            hasAllPermissions = false;
        }

        return hasAllPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allGranted = true;

        Log.d("BluetoothSCO", "onRequestPermissionsResult chamado.");

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            Log.d("BluetoothSCO", "Todas as permissões concedidas. Continuando o processo Bluetooth.");
            startBluetoothProcess();
        } else {
            Log.d("Permissions", "Permissão negada. Não é possível continuar.");
        }
    }

    private void startBluetoothProcess() {
        Log.d("BluetoothSCO", "Iniciando o processo Bluetooth.");

        // Verifique se o Bluetooth está ativado e se há dispositivos emparelhados
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Log.d("BluetoothSCO", "Bluetooth está ativado.");

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                Log.d("BluetoothSCO", "Dispositivos emparelhados encontrados.");

                for (BluetoothDevice device : pairedDevices) {
                    Log.d("BluetoothSCO", "Dispositivo emparelhado: " + device.getName());

                    // Verifique se o dispositivo emparelhado é o fone Bluetooth F9-5C
                    if (device.getName().equals(TARGET_DEVICE_NAME)) {
                        Log.d("BluetoothSCO", "Fone de ouvido Bluetooth encontrado: " + device.getName());
                        startBluetoothSco();
                        break;
                    }
                }
            } else {
                Log.d("BluetoothSCO", "Nenhum dispositivo Bluetooth emparelhado foi encontrado.");
            }
        } else {
            Log.d("BluetoothSCO", "Bluetooth não está ativado ou adaptador não está disponível.");
        }
    }

    private void startBluetoothSco() {
        Log.d("BluetoothSCO", "Entrando no método startBluetoothSco.");

        if (audioManager != null) {
            // Verifique se o SCO já está ligado
            if (!audioManager.isBluetoothScoOn()) {
                try {
                    Log.d("BluetoothSCO", "Tentando iniciar o Bluetooth SCO...");
                    // Inicia a conexão SCO
                    audioManager.startBluetoothSco();
                    Log.d("BluetoothSCO", "SCO ativação solicitada.");

                    // Adiciona um delay para verificar se o SCO foi ativado
                    new Handler().postDelayed(() -> {
                        if (audioManager.isBluetoothScoOn()) {
                            Log.d("BluetoothSCO", "Bluetooth SCO ativado com sucesso.");
                        } else {
                            Log.d("BluetoothSCO", "Falha ao ativar Bluetooth SCO.");
                        }
                    }, 3000); // 3 segundos de delay

                } catch (SecurityException e) {
                    Log.e("BluetoothSCO", "Erro de permissão ao iniciar Bluetooth SCO: " + e.getMessage());
                }
            } else {
                Log.d("BluetoothSCO", "Bluetooth SCO já está ligado.");
            }

            // Ativa o SCO para captura de áudio
            audioManager.setBluetoothScoOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            Log.d("BluetoothSCO", "AudioManager é nulo. Não foi possível ativar Bluetooth SCO.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void startRecording() {
        Log.d("BluetoothSCO", "Iniciando gravação de áudio...");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings/");
        values.put(MediaStore.Audio.Media.TITLE, "recorded_audio");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");

        try {
            // Cria um Uri e OutputStream no MediaStore
            outputStream = getContentResolver().openOutputStream(
                    getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            );

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(outputStream.getFD());

            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Gravação iniciada", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("BluetoothSCO", "Erro ao preparar MediaRecorder: " + e.getMessage());
        }
    }

    private void stopRecording() {
        Log.d("BluetoothSCO", "Parando gravação de áudio...");

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(this, "Gravação parada. Arquivo salvo com sucesso.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("BluetoothSCO", "onDestroy chamado. Desligando Bluetooth SCO.");
        super.onDestroy();
        stopBluetoothSco();
    }

    private void stopBluetoothSco() {
        Log.d("BluetoothSCO", "Entrando no método stopBluetoothSco.");

        if (audioManager != null) {
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
            Log.d("BluetoothSCO", "Bluetooth SCO desligado.");
        } else {
            Log.d("BluetoothSCO", "AudioManager é nulo. Não foi possível desligar Bluetooth SCO.");
        }
    }
}
