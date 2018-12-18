package com.rightside.meutesoureiro_controlesuacarteira.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.rightside.meutesoureiro_controlesuacarteira.R;
import com.rightside.meutesoureiro_controlesuacarteira.config.ConfiguracaoFirebase;
import com.rightside.meutesoureiro_controlesuacarteira.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button botaoEntrar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        botaoEntrar = findViewById(R.id.buttonEntrar);

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validarLogin();
            }
        });


    }


    public void validarLogin() {

        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoEmail.isEmpty()) {
            if(!textoSenha.isEmpty()){

                usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);
                fazerLogin();


            }else {
                Toast.makeText(LoginActivity.this,"Preencha a senha !", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(LoginActivity.this,"Preencha o Email !", Toast.LENGTH_SHORT).show();
        }

    }


    public void fazerLogin() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(),usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {


                abrirTelaPrincipal();


             } else {
                 String excecao = "";
                 try {
                     throw task.getException(); //tratando exceções
                 } catch (FirebaseAuthInvalidUserException e) {
                     excecao = "Usuario não foi cadastrado";
                 } catch (FirebaseAuthInvalidCredentialsException e) {
                     excecao = "E-mail e senha não correspondem a um usuario cadastrado";
                 }
                 catch (Exception e) {
                     excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                     e.printStackTrace();
                 }
                 Toast.makeText(LoginActivity.this,excecao, Toast.LENGTH_LONG).show();
             }
             }

        });
    }

    public void abrirTelaPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }
}
