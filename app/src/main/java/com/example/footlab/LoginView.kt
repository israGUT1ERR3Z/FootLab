package com.example.footlab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginView : AppCompatActivity() {

    //Declaración e inicialización de atributos//
    private lateinit var User: EditText
    private lateinit var Pass: EditText
    private lateinit var botonLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var username:String
    private lateinit var password:String

    //Función MAIN//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_view)

        //Inicialización de los elementos del layout//
        User=findViewById(R.id.cajaCorreo)
        Pass=findViewById(R.id.cajaPassword)
        botonLogin=findViewById(R.id.botonLogin)

        //Inicialización de Firebase//
        auth=FirebaseAuth.getInstance()
        db=FirebaseFirestore.getInstance()

        //Evento para el botón para iniciar sesión//
        botonLogin.setOnClickListener{
            iniciarSesion()
        }
    }

    //Funcion de inicio de sesión//
    fun iniciarSesion(){
        //Inicialización de variables//
        username=User.text.toString().trim()
        password=Pass.text.toString().trim()

        if(username.isNotEmpty() && password.isNotEmpty()){ //Si el usuario y contraseña no están vacíos//
            //Si el usuario contiene en su cadena un @, va a iniciar sesión con correo, sino, con nombre de usuario//
            if(username.contains("@")){
                login(username,password,"Email")
            }else{
                login(username,password,"UserName")
            }
        }else{
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_LONG).show()
        }
    }

    fun login(usuario:String,contra:String,campo:String){
        /*De la colección "Pacientes", por medio del iterador "documentos" va a recorrer los documentos hasta hallar
          al usuario correcto por medio de un ciclo FOR*/
        db.collection("Pacientes").whereEqualTo(campo, usuario).get().addOnSuccessListener{documentos ->
            if (!documentos.isEmpty){
                for (paciente in documentos.documents){
                    val password = paciente.getString("Contraseña")
                    val nombre = paciente.getString("Nombres")
                    if(password==contra){
                        Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this, "Contraseña incorrecta. Favor de rectificar", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_LONG).show()
            }
        }

    }
}