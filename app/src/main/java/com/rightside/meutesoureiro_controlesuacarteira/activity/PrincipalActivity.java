package com.rightside.meutesoureiro_controlesuacarteira.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.rightside.meutesoureiro_controlesuacarteira.Manifest;
import com.rightside.meutesoureiro_controlesuacarteira.R;
import com.rightside.meutesoureiro_controlesuacarteira.adapter.AdapterMovimentacao;
import com.rightside.meutesoureiro_controlesuacarteira.config.ConfiguracaoFirebase;
import com.rightside.meutesoureiro_controlesuacarteira.helper.Base64Custom;
import com.rightside.meutesoureiro_controlesuacarteira.model.Movimentacao;
import com.rightside.meutesoureiro_controlesuacarteira.model.Usuario;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;



public class PrincipalActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textoSaudacao, textoSaldo;
    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;
    private AdView mAdView;

    private InterstitialAd mInterstitialAd;
    private InterstitialAd mInterstitialAd2;
    private InterstitialAd mInterstitialAd3;





    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;

    private RecyclerView recyclerView;
    private AdapterMovimentacao adapterMovimentacao;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private Movimentacao movimentacao;
    private DatabaseReference movimentacaoRef;
    private String mesAnoSelecionado;

    private static final int REQUEST_PERMISSAO_ARQUIVOS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Meu Tesoureiro");
        setSupportActionBar(toolbar);


        verificaPermissaoArquivos();


        MobileAds.initialize(this,"ca-app-pub-6000601527647342~5991960457");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
                                          @Override
                                          public void onAdClosed() {

                                              startActivity(new Intent(PrincipalActivity.this, DespesasActivity.class));
                                              mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
                                          }
                                      }




        );


        mInterstitialAd2 = new InterstitialAd(this);
        mInterstitialAd2.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd2.loadAd(new AdRequest.Builder().build());

        mInterstitialAd2.setAdListener(new AdListener() {
                                          @Override
                                          public void onAdClosed() {

                                              startActivity(new Intent(PrincipalActivity.this, ReceitasActivity.class));
                                              mInterstitialAd2.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
                                          }
                                      }




        );

        mInterstitialAd3 = new InterstitialAd(this);
        mInterstitialAd3.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd3.loadAd(new AdRequest.Builder().build());

        mInterstitialAd3.setAdListener(new AdListener() {
                                           @Override
                                           public void onAdClosed() {

                                               gerarRelatorio();
                                               mInterstitialAd2.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
                                           }
                                       }




        );





        textoSaldo = findViewById(R.id.textSaldo);
        textoSaudacao = findViewById(R.id.textSaudacao);
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerMovimentos);

        configuraCalendarView();
        swipe();

        //Configurar adapter
        adapterMovimentacao = new AdapterMovimentacao(movimentacoes,this);

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter( adapterMovimentacao );




    }



    public void swipe(){

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao( viewHolder );
            }
        };

        new ItemTouchHelper( itemTouch ).attachToRecyclerView( recyclerView );

    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        //Configura AlertDialog
        alertDialog.setTitle("Excluir Movimentação da Conta");
        alertDialog.setMessage("Você tem certeza que deseja realmente excluir essa movimentação de sua conta?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get( position );

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64( emailUsuario );
                movimentacaoRef = firebaseRef.child("movimentacao")
                        .child( idUsuario )
                        .child( mesAnoSelecionado );

                movimentacaoRef.child( movimentacao.getKey() ).removeValue();
                adapterMovimentacao.notifyItemRemoved( position );
                atualizarSaldo();

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PrincipalActivity.this,
                        "Cancelado",
                        Toast.LENGTH_SHORT).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();


    }

    public void atualizarSaldo(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        usuarioRef = firebaseRef.child("usuarios").child( idUsuario );

        if ( movimentacao.getTipo().equals("r") ){
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(receitaTotal);
        }

        if ( movimentacao.getTipo().equals("d") ){
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue( despesaTotal );
        }

    }

    public void recuperarMovimentacoes(){

         FirebaseAuth teste = ConfiguracaoFirebase.getFirebaseAutenticacao();

          if (teste.getCurrentUser() != null) {


            String emailUsuario = autenticacao.getCurrentUser().getEmail();
            String idUsuario = Base64Custom.codificarBase64(emailUsuario);
            movimentacaoRef = firebaseRef.child("movimentacao")
                    .child(idUsuario)
                    .child(mesAnoSelecionado);

            valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    movimentacoes.clear();
                    for (DataSnapshot dados : dataSnapshot.getChildren()) {

                        Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                        movimentacao.setKey(dados.getKey());
                        movimentacoes.add(movimentacao);

                    }

                    adapterMovimentacao.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void recuperarResumo() {



        if (autenticacao.getCurrentUser() != null) {

            String emailUsuario = autenticacao.getCurrentUser().getEmail();
            String idUsuario = Base64Custom.codificarBase64(emailUsuario);
            usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

            valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    Usuario usuario = dataSnapshot.getValue(Usuario.class);


                    despesaTotal = 0.00;

                    despesaTotal = usuario.getDespesaTotal();
                    receitaTotal = usuario.getReceitaTotal();
                    resumoUsuario = receitaTotal - despesaTotal;

                    DecimalFormat decimalFormat = new DecimalFormat("0.##");
                    String resultadoFormatado = decimalFormat.format(resumoUsuario);

                    textoSaudacao.setText("Olá, " + usuario.getNome());
                    textoSaldo.setText("R$ " + resultadoFormatado);

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            });

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.menuSugestao:
                composeEmail();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void adicionarDespesa(View view){
        if(mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            startActivity(new Intent(this, DespesasActivity.class));
        }

    }

    public void adicionarReceita(View view){
        if(mInterstitialAd2.isLoaded()) {
            mInterstitialAd2.show();
        } else {
            startActivity(new Intent(this, ReceitasActivity.class));
        }

    }



    public void configuraCalendarView(){

        CharSequence meses[] = {"Janeiro","Fevereiro", "Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        calendarView.setTitleMonths( meses );

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", (dataAtual.getMonth() + 1) );
        mesAnoSelecionado = String.valueOf( mesSelecionado + "" + dataAtual.getYear() );

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format("%02d", (date.getMonth() + 1) );
                mesAnoSelecionado = String.valueOf( mesSelecionado + "" + date.getYear() );

                movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );
                recuperarMovimentacoes();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener( valueEventListenerUsuario );
        movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );
    }


    private void gerarRelatorio() {



        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        Double nomeUsuario = resumoUsuario;
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        movimentacaoRef = firebaseRef.child("movimentacao")
                .child(idUsuario)
                .child(mesAnoSelecionado);


        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

            if (autenticacao.getCurrentUser()!=null) {

                if (movimentacaoRef != null) {



                    String arquivo_gerado = Environment.getExternalStorageDirectory().toString() + "/relatorio.pdf";


                    Document doc = null;
                    OutputStream os = null;

                    try {
                        doc = new Document(PageSize.A4, 42, 42, 42, 42);

                        os = new FileOutputStream(arquivo_gerado);
                        PdfWriter.getInstance(doc, os);
                        doc.open();
                        Paragraph h = new Paragraph("RELATÓRIO TOTAL MEU TESOUREIRO - CONTROLE SUA CARTEIRA " +
                                "");
                        Paragraph l = new Paragraph("Despesa total até o momento: " + String.valueOf(despesaTotal));
                        Paragraph z = new Paragraph("Receita total até o momento: " + String.valueOf(receitaTotal));


                        Paragraph g = new Paragraph("Email: " + emailUsuario);
                        Paragraph i = new Paragraph("Balanço Total " + nomeUsuario);



                        doc.add(h);
                        doc.add(g);
                        doc.add(i);
                        doc.add(z);
                        doc.add(l);
                        doc.close();
                        os.close();

                        Toast.makeText(getBaseContext(), "Relatorio gerado, salvo no dispositivo", Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException erro) {
                        Toast.makeText(getBaseContext(), "Erro: " + erro, Toast.LENGTH_LONG).show();
                    } catch (DocumentException erro) {
                        Toast.makeText(getBaseContext(), "Erro: " + erro, Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        Toast.makeText(getBaseContext(), "Erro: " + erro, Toast.LENGTH_LONG).show();
                    }


                }
            }

    }

    public void fazerRelatorio(View view) {

        if(mInterstitialAd3.isLoaded()) {
            mInterstitialAd3.show();
        } else {
            gerarRelatorio();
        }




    }

    public void composeEmail() {

        String to = "contatorightside@gmail.com";
        String subject = "Gostaria de enviar uma sugestão/critica para o aplicativo Meu Tesoureiro";
        String message = "Oi me chamo   , gostaria que fosse feito...";

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{ to});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);
        email.setType("message/rfc822");

        startActivity(Intent.createChooser(email, "Escolha aplicativo de e-mail!"));
    }

    public void verificaPermissaoArquivos() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSAO_ARQUIVOS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_PERMISSAO_ARQUIVOS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getBaseContext(), "Permissão para arquivos foi negada", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }

    }
}


