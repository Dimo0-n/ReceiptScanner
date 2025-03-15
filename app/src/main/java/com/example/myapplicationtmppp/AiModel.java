package com.example.myapplicationtmppp;

import android.content.Context;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import java.io.IOException;
import java.nio.MappedByteBuffer;

public class AiModel {

    private Interpreter tflite;

    public AiModel(Context context) {
        try {
            // Încarcă modelul .tflite din folderul assets
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, "1.tflite");
            tflite = new Interpreter(modelBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Eroare la încărcarea modelului TFLite", e);
        }
    }

    public float[] predict(float[] input) {
        // Verifică dacă interpreterul este inițializat corect
        if (tflite == null) {
            throw new RuntimeException("Interpreterul TFLite nu a fost inițializat corect.");
        }

        // Pregătește output-ul
        float[][] output = new float[1][1]; // Modifică dimensiunea în funcție de modelul tău

        // Rulează inferența
        tflite.run(input, output);

        // Returnează rezultatul
        return output[0];
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}