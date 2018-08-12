package neto.com.mx.conteocedisciclicos.dialogos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import neto.com.mx.conteocedisciclicos.R;
import java.math.BigDecimal;
import java.util.Locale;

import neto.com.mx.conteocedisciclicos.globales.GlobalShare;


/**
 * Created by dramirezr on 07/02/2018.
 */


public class CalculadoraActivity extends AppCompatActivity {

    StringBuilder strBuilder;
    TextView txtResultado, txtExpresion;
    Button cancelar,
            aceptar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        txtResultado = (TextView)findViewById(R.id.txtResultado);
        txtExpresion = (TextView)findViewById(R.id.txtExpresion);

        cancelar= (Button) findViewById(R.id.btnCerrar);
        aceptar = (Button) findViewById(R.id.btnAceptar);

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        strBuilder = new StringBuilder();
    }

    public void borrarCaracter(View view){
        try {
            if( strBuilder.length() > 0) {
                strBuilder.deleteCharAt(strBuilder.length() - 1);
            }
            txtExpresion.setText(strBuilder.toString());
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName() +
                    " : borrarCaracter > " + e.getMessage(), e);
        }
        evaluarExpresion();
    }

    public void igual(View v){
        try {
            String expresion = txtExpresion.getText().toString();

            Log.d(GlobalShare.logAplicaion, getClass().getName() +
                    " : evaluarExpresion > Expresion: " + expresion);

            double resultado = evaluar(expresion);
            txtResultado.setText("Resultado    >>    "+String.format("%.0f",resultado) );
            txtExpresion.setText("");

            if( strBuilder.length() > 0)
                strBuilder.delete(0, strBuilder.length());

            //strBuilder.append(txtExpresion.getText().toString());
        }catch (Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName() +
                    " : evaluarExpresion > " + e.getMessage(), e);
        }
    }

    public void agregarSignoExpresion(View view){
        String nombreView = getResources().getResourceEntryName(view.getId());
        String exp = nombreView.replace("button","");
        switch (exp){
            case "Mas": strBuilder.append("+");
                break;
            case "Menos":strBuilder.append("-");
                break;
            case "x": strBuilder.append("*");
                break;
        }

        txtExpresion.setText( strBuilder.toString() );
        //evaluarExpresion();
    }

    public void agregarTextoExpresion(View view){
        String nombreView = getResources().getResourceEntryName(view.getId());
        String exp = nombreView.replace("button","");
        strBuilder.append(exp);

        txtExpresion.setText( strBuilder.toString() );
        //evaluarExpresion();
    }

    private void evaluarExpresion(){
        try {
            String expresion = txtExpresion.getText().toString();

            Log.d(GlobalShare.logAplicaion, getClass().getName() +
                    " : evaluarExpresion > Expresion: " + expresion);

            if( !expresion.isEmpty() ) {
                double resultado = evaluar(expresion);
                txtResultado.setText(String.format("%.0f", resultado));
            }else{
                txtResultado.setText("0");
            }
        }catch (Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName() +
                    " : evaluarExpresion > " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( strBuilder.length() > 0)
            strBuilder.replace(0, strBuilder.length()-1, "");
    }


    public static double evaluar(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else if (eat('^')) x = Math.pow(x, parseFactor()); //exponentiation -> Moved in to here. So the problem is fixed
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                //if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation -> This is causing a bit of problem

                return x;
            }
        }.parse();
    }
}
