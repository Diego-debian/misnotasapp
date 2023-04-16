package unal.todosalau.misnotas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ArrayList<String> titulosNotas;
    private ArrayList<Date> fechasNotas;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inicializa los ArrayList y el ArrayAdapter para la lista de notas
        titulosNotas = new ArrayList<>();
        fechasNotas = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, titulosNotas);

        // Asigna el ArrayAdapter a la lista de notas
        ListView lvNotas = findViewById(R.id.lvNotas);
        lvNotas.setAdapter(adapter);

        // Recupera todas las notas de Cloud Firestore y las agrega a la lista
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notasRef = db.collection("misnotas");

        notasRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String titulo = documentSnapshot.getString("titulo");
                        String contenido = documentSnapshot.getString("contenido");
                        Date fecha = documentSnapshot.getDate("fecha");
                        titulosNotas.add(titulo);
                        fechasNotas.add(fecha);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al recuperar las notas: " + e.getMessage());
                    Toast.makeText(this, "Error al recuperar las notas", Toast.LENGTH_SHORT).show();
                });

        // Agrega una nueva nota cuando se presiona el botón "Agregar nota"
        Button btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(v -> {
            String nuevaNota = ((TextInputEditText) findViewById(R.id.entradaNota)).getText().toString();
            Map<String, Object> nota = new HashMap<>();
            nota.put("titulo", nuevaNota);
            nota.put("contenido", "");
            nota.put("fecha", new Date());

            notasRef.add(nota)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Nota agregada con ID: " + documentReference.getId());
                        Toast.makeText(this, "Nota agregada", Toast.LENGTH_SHORT).show();
                        titulosNotas.add(nuevaNota);
                        fechasNotas.add(new Date());
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al agregar la nota: " + e.getMessage());
                        Toast.makeText(this, "Error al agregar la nota", Toast.LENGTH_SHORT).show();
                    });
        });

        // Muestra el título y la fecha de la nota seleccionada en un Toast
        lvNotas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String titulo = titulosNotas.get(position);
                Date fecha = fechasNotas.get(position);
                Toast.makeText(MainActivity.this, titulo + "\n" + fecha.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}