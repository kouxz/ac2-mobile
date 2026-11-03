package com.projeto.ac2;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTitulo, editAutor, editAno;
    private Spinner spinnerGenero, spinnerStatus, spinnerFiltroGenero;
    private Switch switchFavorito;
    private Button btnSalvar;
    private TextView tvTotalLivros;

    private RecyclerView recyclerView;
    private LivroAdapter adaptador;
    private List<Livro> listaLivros = new ArrayList<>();
    private List<Livro> listaFiltrada = new ArrayList<>();

    private FirebaseFirestore db;
    private CollectionReference livrosRef;
    private String idLivroEmEdicao = null;

    private final String[] generosDisponiveis = {"Selecione o Gênero", "Romance", "Fantasia", "Terror", "Ficção Científica", "Biografia"};
    private final String[] statusDisponiveis = {"Selecione o Status", "Quero ler", "Lendo", "Concluído"};
    private final String[] filtrosDeGenero = {"Todos os Gêneros", "Romance", "Fantasia", "Terror", "Ficção Científica", "Biografia"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance();
        livrosRef = db.collection("livros");

        // Mapeia Elementos da Interface
        editTitulo = findViewById(R.id.editTitulo);
        editAutor = findViewById(R.id.editAutor);
        editAno = findViewById(R.id.editAno);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerFiltroGenero = findViewById(R.id.spinnerFiltroGenero);
        switchFavorito = findViewById(R.id.switchFavorito);
        btnSalvar = findViewById(R.id.btnSalvar);
        tvTotalLivros = findViewById(R.id.tvTotalLivros);
        recyclerView = findViewById(R.id.recyclerViewLivros);

        configurarSpinners();
        configurarRecyclerView();

        btnSalvar.setOnClickListener(v -> salvarLivro());

        // Carrega dados do Firestore em tempo real
        livrosRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MainActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                    return;
                }

                listaLivros.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Livro livro = doc.toObject(Livro.class);
                        // Garante que o ID do documento seja setado no objeto
                        livro.setId(doc.getId());
                        listaLivros.add(livro);
                    }
                }
                aplicarFiltro(spinnerFiltroGenero.getSelectedItem().toString());
            }
        });
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adaptadorGenero = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, generosDisponiveis);
        spinnerGenero.setAdapter(adaptadorGenero);

        ArrayAdapter<String> adaptadorStatus = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusDisponiveis);
        spinnerStatus.setAdapter(adaptadorStatus);

        ArrayAdapter<String> adaptadorFiltro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filtrosDeGenero);
        spinnerFiltroGenero.setAdapter(adaptadorFiltro);

        spinnerFiltroGenero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> pai, View visualizacao, int posicao, long id) {
                aplicarFiltro(filtrosDeGenero[posicao]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> pai) {}
        });
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new LivroAdapter(listaFiltrada, new LivroAdapter.AoClicarNoItem() {
            @Override
            public void aoClicar(Livro livro) {
                carregarLivroParaEdicao(livro);
            }

            @Override
            public void aoClicarLongo(Livro livro) {
                exibirDialogoExclusao(livro);
            }
        });
        recyclerView.setAdapter(adaptador);
    }

    private void salvarLivro() {
        String titulo = editTitulo.getText().toString().trim();
        String autor = editAutor.getText().toString().trim();
        String anoTexto = editAno.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();
        boolean isFavorito = switchFavorito.isChecked();

        // Validações
        if (titulo.isEmpty()) { editTitulo.setError("O título é obrigatório"); return; }
        if (autor.isEmpty()) { editAutor.setError("O autor é obrigatório"); return; }
        if (anoTexto.isEmpty()) { editAno.setError("O ano é obrigatório"); return; }
        if (spinnerGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione um gênero válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerStatus.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione um status válido", Toast.LENGTH_SHORT).show();
            return;
        }

        int anoPublicacao = Integer.parseInt(anoTexto);

        if (idLivroEmEdicao == null) {
            // Novo Cadastro no Firestore
            String novoId = livrosRef.document().getId();
            Livro novoLivro = new Livro(novoId, titulo, autor, genero, anoPublicacao, status, isFavorito);

            livrosRef.document(novoId).set(novoLivro)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Livro cadastrado!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
        } else {
            // Edição de Cadastro Existente
            Livro livroAtualizado = new Livro(idLivroEmEdicao, titulo, autor, genero, anoPublicacao, status, isFavorito);
            livrosRef.document(idLivroEmEdicao).set(livroAtualizado)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Livro atualizado!", Toast.LENGTH_SHORT).show();
                        idLivroEmEdicao = null;
                        btnSalvar.setText("Salvar Livro");
                    });
        }

        limparCampos();
    }

    private void carregarLivroParaEdicao(Livro livro) {
        idLivroEmEdicao = livro.getId();
        editTitulo.setText(livro.getTitulo());
        editAutor.setText(livro.getAutor());
        editAno.setText(String.valueOf(livro.getAnoPublicacao()));
        switchFavorito.setChecked(livro.isFavorito());
        btnSalvar.setText("Atualizar Livro");

        for (int i = 0; i < generosDisponiveis.length; i++) {
            if (generosDisponiveis[i].equals(livro.getGenero())) spinnerGenero.setSelection(i);
        }
        for (int i = 0; i < statusDisponiveis.length; i++) {
            if (statusDisponiveis[i].equals(livro.getStatusLeitura())) spinnerStatus.setSelection(i);
        }
    }

    private void exibirDialogoExclusao(Livro livro) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Livro")
                .setMessage("Tem certeza que deseja excluir o livro '" + livro.getTitulo() + "'?")
                .setPositiveButton("Sim", (dialogo, qual) -> {
                    livrosRef.document(livro.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Livro excluído!", Toast.LENGTH_SHORT).show());

                    if (livro.getId().equals(idLivroEmEdicao)) {
                        limparCampos();
                        idLivroEmEdicao = null;
                        btnSalvar.setText("Salvar Livro");
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void aplicarFiltro(String generoFiltro) {
        listaFiltrada.clear();
        if (generoFiltro.equals("Todos os Gêneros")) {
            listaFiltrada.addAll(listaLivros);
        } else {
            for (Livro livro : listaLivros) {
                if (livro.getGenero().equals(generoFiltro)) {
                    listaFiltrada.add(livro);
                }
            }
        }
        adaptador.atualizarLista(listaFiltrada);
        tvTotalLivros.setText("Total de livros: " + listaFiltrada.size());
    }

    private void limparCampos() {
        editTitulo.setText("");
        editAutor.setText("");
        editAno.setText("");
        spinnerGenero.setSelection(0);
        spinnerStatus.setSelection(0);
        switchFavorito.setChecked(false);
    }
}