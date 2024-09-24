package com.example.footlab

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HistorialClinicoFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editTextNombres: EditText
    private lateinit var editTextApellidos: EditText
    private lateinit var editTextEdad: EditText
    private lateinit var editTextTalla: EditText
    private lateinit var editTextPeso: EditText
    private lateinit var editTextIMC: EditText
    private lateinit var editTextTemperatura: EditText
    private lateinit var editTextFrecuenciaRespiratoria: EditText
    private lateinit var editTextFrecuenciaCardiaca: EditText
    private lateinit var editTextTensionArterial: EditText
    private lateinit var editTextTabaquismo: EditText
    private lateinit var editTextAlcoholismo: EditText
    private lateinit var editTextSedentarismo: EditText
    private lateinit var editTextHabitosAlimenticios: EditText
    private lateinit var editTextTipoDiabetes: EditText
    private lateinit var editTextHipertensionArterial: EditText
    private lateinit var editTextDislipidemia: EditText
    private lateinit var editTextObesidad: EditText
    private lateinit var editTextControlGlicemico: EditText
    private lateinit var editTextManejoPieDiabetico: EditText
    private lateinit var editTextControlComorbilidades: EditText
    private lateinit var buttonGuardar: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial_clinico, container, false)

        // Inicialización de los EditTexts
        editTextNombres = view.findViewById(R.id.editTextNombres)
        editTextApellidos = view.findViewById(R.id.editTextApellidos)
        editTextEdad = view.findViewById(R.id.editTextEdad)
        editTextTalla = view.findViewById(R.id.editTextTalla)
        editTextPeso = view.findViewById(R.id.editTextPeso)
        editTextIMC = view.findViewById(R.id.editTextIMC)
        editTextTemperatura = view.findViewById(R.id.editTextTemperatura)
        editTextFrecuenciaRespiratoria = view.findViewById(R.id.editTextFrecuenciaRespiratoria)
        editTextFrecuenciaCardiaca = view.findViewById(R.id.editTextFrecuenciaCardiaca)
        editTextTensionArterial = view.findViewById(R.id.editTextTensionArterial)
        editTextTabaquismo = view.findViewById(R.id.editTextTabaquismo)
        editTextAlcoholismo = view.findViewById(R.id.editTextAlcoholismo)
        editTextSedentarismo = view.findViewById(R.id.editTextSedentarismo)
        editTextHabitosAlimenticios = view.findViewById(R.id.editTextHabitosAlimenticios)
        editTextTipoDiabetes = view.findViewById(R.id.editTextTipoDiabetes)
        editTextHipertensionArterial = view.findViewById(R.id.editTextHipertensionArterial)
        editTextDislipidemia = view.findViewById(R.id.editTextDislipidemia)
        editTextObesidad = view.findViewById(R.id.editTextObesidad)
        editTextControlGlicemico = view.findViewById(R.id.editTextControlGlicemico)
        editTextManejoPieDiabetico = view.findViewById(R.id.editTextManejoPieDiabetico)
        editTextControlComorbilidades = view.findViewById(R.id.editTextControlComorbilidades)
        buttonGuardar = view.findViewById(R.id.buttonGuardar)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val sharedPreferences2 = requireContext().getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences2.getString("Username", null)

        if (username != null) {
            isHistorialClinicoVacio(username) { vacio ->
                if (vacio) {
                    mostrarDialogoCreacion()
                } else {
                    cargarHistorialClinico(username)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

        buttonGuardar.setOnClickListener{
            guardarHistorialClinico()
        }


        return view
    }


    private fun isHistorialClinicoVacio(username: String, callback: (Boolean) -> Unit){

        if (username != null) {
            val campo = if (username.contains("@")) "Email" else "UserName"
            val docRef = firestore.collection("Pacientes").whereEqualTo(campo, username)
            docRef.get().addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    val documento = documentos.documents[0] // Solo tomamos el primer documento
                    val datos = documento.data ?: emptyMap()

                    // Validar si alguno de los campos no existe o está vacío
                    val camposRequeridos = listOf(
                        "Edad", "Talla", "Peso", "IMC", "Temperatura", "FrecuenciaRespiratoria",
                        "FrecuenciaCardiaca", "TensionArterial", "Tabaquismo", "Alcoholismo",
                        "Sedentarismo", "HabitosAlimenticios", "TipoDiabetes", "HipertensionArterial",
                        "Dislipidemia", "Obesidad", "ControlGlicemico", "ManejoPieDiabetico", "ControlComorbilidades"
                    )

                    val historialVacio = camposRequeridos.any { campo ->
                        datos[campo] == null || (datos[campo] as? String).isNullOrEmpty()
                    }

                    callback(historialVacio)
                } else {
                    // Si no se encontró el paciente, consideramos el historial como vacío
                    callback(true)
                }
            }.addOnFailureListener { exception ->
                // Si ocurre un error, consideramos el historial como vacío y mostramos un mensaje
                Toast.makeText(requireContext(), "Error al consultar el historial: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(true)
            }
        }

    }

    private fun cargarHistorialClinico(username: String) {
        val campo = if (username.contains("@")) "Email" else "UserName"

        // Consulta a Firestore para obtener el historial clínico del paciente
        firestore.collection("Pacientes").whereEqualTo(campo, username)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    val documento = documentos.documents[0] // Tomamos el primer documento
                    val datos = documento.data ?: emptyMap()

                    // Recuperar los datos del documento y asignarlos a los EditText
                    editTextNombres.setText(datos["Nombres"] as? String ?: "")
                    editTextApellidos.setText(datos["Apellidos"] as? String ?: "")
                    editTextEdad.setText(datos["Edad"] as? String ?: "")
                    editTextTalla.setText(datos["Talla"] as? String ?: "")
                    editTextPeso.setText(datos["Peso"] as? String ?: "")
                    editTextIMC.setText(datos["IMC"] as? String ?: "")
                    editTextTemperatura.setText(datos["Temperatura"] as? String ?: "")
                    editTextFrecuenciaRespiratoria.setText(datos["FrecuenciaRespiratoria"] as? String ?: "")
                    editTextFrecuenciaCardiaca.setText(datos["FrecuenciaCardiaca"] as? String ?: "")
                    editTextTensionArterial.setText(datos["TensionArterial"] as? String ?: "")
                    editTextTabaquismo.setText(datos["Tabaquismo"] as? String ?: "")
                    editTextAlcoholismo.setText(datos["Alcoholismo"] as? String ?: "")
                    editTextSedentarismo.setText(datos["Sedentarismo"] as? String ?: "")
                    editTextHabitosAlimenticios.setText(datos["HabitosAlimenticios"] as? String ?: "")
                    editTextTipoDiabetes.setText(datos["TipoDiabetes"] as? String ?: "")
                    editTextHipertensionArterial.setText(datos["HipertensionArterial"] as? String ?: "")
                    editTextDislipidemia.setText(datos["Dislipidemia"] as? String ?: "")
                    editTextObesidad.setText(datos["Obesidad"] as? String ?: "")
                    editTextControlGlicemico.setText(datos["ControlGlicemico"] as? String ?: "")
                    editTextManejoPieDiabetico.setText(datos["ManejoPieDiabetico"] as? String ?: "")
                    editTextControlComorbilidades.setText(datos["ControlComorbilidades"] as? String ?: "")

                    // Deshabilitar campos si es necesario
                    deshabilitarCampos()
                    buttonGuardar.visibility=View.INVISIBLE
                } else {
                    Toast.makeText(requireContext(), "No se encontró el historial clínico", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al cargar el historial clínico: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

    }



    private fun mostrarDialogoCreacion() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Historial Clínico")
        dialog.setMessage("No existe un historial clínico. ¿Desea crear uno?")
        dialog.setPositiveButton("Si") { _, _ ->
            habilitarCampos()
        }
        dialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            val fragment = HistorialFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit() }
        dialog.show()
    }

    private fun guardarHistorialClinico() {
        val sharedPreferences2 = requireContext().getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences2.getString("Username", null)

        if (username != null) {
            val campo = if (username.contains("@")) "Email" else "UserName"

            // Busca el documento correspondiente
            firestore.collection("Pacientes").whereEqualTo(campo, username)
                .get()
                .addOnSuccessListener { documentos ->
                    if (documentos.isEmpty) {
                        // Si no existe, crear el documento
                        val newDocRef = firestore.collection("Pacientes").document()
                        val datos = hashMapOf(
                            "Nombres" to editTextNombres.text.toString(),
                            "Apellidos" to editTextApellidos.text.toString(),
                            "Edad" to editTextEdad.text.toString(),
                            "Talla" to editTextTalla.text.toString(),
                            "Peso" to editTextPeso.text.toString(),
                            "IMC" to editTextIMC.text.toString(),
                            "Temperatura" to editTextTemperatura.text.toString(),
                            "FrecuenciaRespiratoria" to editTextFrecuenciaRespiratoria.text.toString(),
                            "FrecuenciaCardiaca" to editTextFrecuenciaCardiaca.text.toString(),
                            "TensionArterial" to editTextTensionArterial.text.toString(),
                            "Tabaquismo" to editTextTabaquismo.text.toString(),
                            "Alcoholismo" to editTextAlcoholismo.text.toString(),
                            "Sedentarismo" to editTextSedentarismo.text.toString(),
                            "HabitosAlimenticios" to editTextHabitosAlimenticios.text.toString(),
                            "TipoDiabetes" to editTextTipoDiabetes.text.toString(),
                            "HipertensionArterial" to editTextHipertensionArterial.text.toString(),
                            "Dislipidemia" to editTextDislipidemia.text.toString(),
                            "Obesidad" to editTextObesidad.text.toString(),
                            "ControlGlicemico" to editTextControlGlicemico.text.toString(),
                            "ManejoPieDiabetico" to editTextManejoPieDiabetico.text.toString(),
                            "ControlComorbilidades" to editTextControlComorbilidades.text.toString()
                        )

                        newDocRef.set(datos)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Historial clínico creado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Error al crear el historial: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // Si existe, actualizar los campos
                        val docRef = documentos.documents[0].reference
                        val datos = HashMap<String, Any>()

                        datos["Nombres"] = editTextNombres.text.toString()
                        datos["Apellidos"] = editTextApellidos.text.toString()
                        datos["Edad"] = editTextEdad.text.toString()
                        datos["Talla"] = editTextTalla.text.toString()
                        datos["Peso"] = editTextPeso.text.toString()
                        datos["IMC"] = editTextIMC.text.toString()
                        datos["Temperatura"] = editTextTemperatura.text.toString()
                        datos["FrecuenciaRespiratoria"] = editTextFrecuenciaRespiratoria.text.toString()
                        datos["FrecuenciaCardiaca"] = editTextFrecuenciaCardiaca.text.toString()
                        datos["TensionArterial"] = editTextTensionArterial.text.toString()
                        datos["Tabaquismo"] = editTextTabaquismo.text.toString()
                        datos["Alcoholismo"] = editTextAlcoholismo.text.toString()
                        datos["Sedentarismo"] = editTextSedentarismo.text.toString()
                        datos["HabitosAlimenticios"] = editTextHabitosAlimenticios.text.toString()
                        datos["TipoDiabetes"] = editTextTipoDiabetes.text.toString()
                        datos["HipertensionArterial"] = editTextHipertensionArterial.text.toString()
                        datos["Dislipidemia"] = editTextDislipidemia.text.toString()
                        datos["Obesidad"] = editTextObesidad.text.toString()
                        datos["ControlGlicemico"] = editTextControlGlicemico.text.toString()
                        datos["ManejoPieDiabetico"] = editTextManejoPieDiabetico.text.toString()
                        datos["ControlComorbilidades"] = editTextControlComorbilidades.text.toString()

                        docRef.update(datos)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Historial clínico actualizado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Error al actualizar el historial: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error al buscar el paciente: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            deshabilitarCampos()
            val fragment = HistorialFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        }
    }

    private fun habilitarCampos() {
        // Habilitar campos para permitir entrada de datos
        editTextNombres.isEnabled = true
        editTextApellidos.isEnabled = true
        editTextEdad.isEnabled = true
        editTextTalla.isEnabled = true
        editTextPeso.isEnabled = true
        editTextIMC.isEnabled = true
        editTextTemperatura.isEnabled = true
        editTextFrecuenciaRespiratoria.isEnabled = true
        editTextFrecuenciaCardiaca.isEnabled = true
        editTextTensionArterial.isEnabled = true
        editTextTabaquismo.isEnabled = true
        editTextAlcoholismo.isEnabled = true
        editTextSedentarismo.isEnabled = true
        editTextHabitosAlimenticios.isEnabled = true
        editTextTipoDiabetes.isEnabled = true
        editTextHipertensionArterial.isEnabled = true
        editTextDislipidemia.isEnabled = true
        editTextObesidad.isEnabled = true
        editTextControlGlicemico.isEnabled = true
        editTextManejoPieDiabetico.isEnabled = true
        editTextControlComorbilidades.isEnabled = true
    }

    private fun deshabilitarCampos() {
        editTextNombres.isEnabled = false
        editTextApellidos.isEnabled = false
        editTextEdad.isEnabled = false
        editTextTalla.isEnabled = false
        editTextPeso.isEnabled = false
        editTextIMC.isEnabled = false
        editTextTemperatura.isEnabled = false
        editTextFrecuenciaRespiratoria.isEnabled = false
        editTextFrecuenciaCardiaca.isEnabled = false
        editTextTensionArterial.isEnabled = false
        editTextTabaquismo.isEnabled = false
        editTextAlcoholismo.isEnabled = false
        editTextSedentarismo.isEnabled = false
        editTextHabitosAlimenticios.isEnabled = false
        editTextTipoDiabetes.isEnabled = false
        editTextHipertensionArterial.isEnabled = false
        editTextDislipidemia.isEnabled = false
        editTextObesidad.isEnabled = false
        editTextControlGlicemico.isEnabled = false
        editTextManejoPieDiabetico.isEnabled = false
        editTextControlComorbilidades.isEnabled = false
    }

}
