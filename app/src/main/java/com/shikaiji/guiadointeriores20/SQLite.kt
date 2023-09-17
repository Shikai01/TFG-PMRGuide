package com.shikaiji.guiadointeriores20



import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.Serializable
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.round


class SQLite(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(p0: SQLiteDatabase?) {
        // Tabla para administrar los usuarios
        p0?.execSQL("create table Usuarios (Usuario TEXT primary key, contraseña TEXT, email TEXT, dc INTEGER, mr INTEGER, pasos REAL)")

        // Tabla para los edificios
        p0?.execSQL("create table Edificios (IDEdificio INTEGER primary key, Usuario TEXT, NombreE TEXT, Calle TEXT, FOREIGN KEY(Usuario) REFERENCES Usuarios(Usuario) ON DELETE CASCADE)")

        // Tabla para las salas
        p0?.execSQL("create table Salas (IDSala INTEGER primary key, IDEdificio INTEGER, NombreS TEXT, CoordenadaX INTEGER, CoordenadaY INTEGER, FOREIGN KEY(IDEdificio) REFERENCES Edificios(IDEdificio) ON DELETE CASCADE)")

        // Tabla para las conexiones
        p0?.execSQL("CREATE TABLE Conexiones (IDconexion INTEGER PRIMARY KEY, IDSalaO INTEGER, IDSalaD INTEGER, FOREIGN KEY(IDSalaO) REFERENCES Salas(IDSala) ON DELETE CASCADE, FOREIGN KEY(IDSalaD) REFERENCES Salas(IDSala) ON DELETE CASCADE)")

        // Tabla para las instrucciones
        p0?.execSQL("CREATE TABLE Pasos (IDPaso INTEGER PRIMARY KEY, IDconexion INTEGER, Orden INTEGER, Instruccion TEXT, FOREIGN KEY(IDconexion) REFERENCES Conexiones(IDconexion) ON DELETE CASCADE)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }









    //InsertOrUpdate
    //Agrega un usuario
    fun insertOrUpdateUsuario(usuario: String, contrasena: String, email: String, dc: Boolean, mr: Boolean): Int {
        val contentValues = ContentValues().apply {
            put("Usuario", usuario)
            put("contraseña", contrasena)
            put("email", email)
            put("dc", dc)
            put("mr", mr)
            put("pasos", 0.76)
        }
        val db = writableDatabase

        if (!getRowIdForUsuario(usuario)) {
            db.insert("Usuarios", null, contentValues)
        } else {
            return -1
        }
        return 0
    }

    //Agrega los pasos personalizados a cada usuario
    fun insertOrUpdateUsuarioPaso(usuario: String, pasos: Double) {
        val contentValues = ContentValues().apply {
            put("pasos", pasos)
        }
        val db = writableDatabase
        db.update("Usuarios", contentValues, "Usuario = ?", arrayOf(usuario))
    }

    //Agrega una nueva entrada de edificio, devuelve -1 si ya esta en el sistema el edificio
    fun insertOrUpdateEdificio(usuario: String, edificio: String, calle: String): Int {
        val contentValues = ContentValues().apply {
            put("Usuario", usuario)
            put("NombreE", edificio)
            put("Calle", calle)
        }
        val existingRowId = getRowIdForEdificio(usuario, edificio, calle)
        if (existingRowId == -1L) {
            val db = writableDatabase
            val id = generateUniqueEdificioId(db)
            contentValues.put("IDEdificio", id)
            db.insert("Edificios", null, contentValues)
        } else {
            return -1
        }
        return 0
    }

    fun insertSala(nombreSala: String, idEdificio: Int , x:Int, y:Int): Int {
        val db = writableDatabase
        val cursor = db.query(
            "Salas",
            arrayOf("IDSala"),
            "NombreS = ? AND IDEdificio = ?",
            arrayOf(nombreSala, idEdificio.toString()),
            null,
            null,
            null
        )
        if (cursor.count > 0) {
            cursor.close()
            return -1 // Sala ya existe
        }
        // Generar una ID única para el campo de ID de sala
        val uniqueSalaId = generateUniqueSalaId(db)

        // Insertar la nueva sala en la base de datos
        val contentValues = ContentValues().apply {
            put("IDSala", uniqueSalaId)
            put("IDEdificio", idEdificio)
            put("NombreS", nombreSala)
            put("CoordenadaX", x)
            put("CoordenadaY", y)
        }

        db.insert("Salas", null, contentValues)
        cursor.close()

        return 0 // Sala insertada exitosamente
    }

    fun insertConexion(SalaD: String,id_SalaO: String, IdEdificio: String): Int {
        val db = writableDatabase


        val idSalaD= getIDConexion(SalaD, IdEdificio)

        if(idSalaD=="-1"){
            return -2
        }else if(idSalaD==id_SalaO){
            return -3
        }
        val cursor = db.query(
            "Conexiones",
            arrayOf("IDconexion"),
            "IDSalaO = ? AND IDSalaD = ?",
            arrayOf(id_SalaO, idSalaD),
            null,
            null,
            null
        )
        if (cursor.count > 0) {
            cursor.close()
            return -1
        }

        val uniqueConexionId = generateUniqueConexion(db)

        // Insertar la nueva sala en la base de datos
        val contentValues = ContentValues().apply {
            put("IDconexion", uniqueConexionId)
            put("IDSalaO", id_SalaO)
            put("IDSalaD", idSalaD)
        }

        db.insert("Conexiones", null, contentValues)
        cursor.close()

        return 0
    }

    @SuppressLint("Range")
    fun insertPaso(IDConexion: String, Instruccion: String): Int {
        val db = writableDatabase
        val query = """
        SELECT MAX(Orden) AS MayorOrden
        FROM Pasos
        WHERE IDconexion = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(IDConexion))

        val mayorOrden: Int = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex("MayorOrden"))
        } else {
            0
        }
        cursor.close()

        val nuevoOrden = mayorOrden + 1
        val uniquePasoId = generateUniquePaso(db)
        // Insertar la nueva sala en la base de datos
        val contentValues = ContentValues().apply {
            put("IDPaso", uniquePasoId)
            put("IDconexion", IDConexion.toInt())
            put("Orden", nuevoOrden)
            put("Instruccion", Instruccion)
        }

        db.insert("Pasos", null, contentValues)

        return 0
    }













    //Generator de IDs Unics
    private fun generateUniqueEdificioId(db: SQLiteDatabase): Int {
        val cursor = db.rawQuery("SELECT MAX(IDEdificio) FROM Edificios", null)
        cursor.moveToFirst()
        val maxId = cursor.getInt(0)
        cursor.close()
        return maxId + 1
    }
    private fun generateUniqueSalaId(db: SQLiteDatabase): Long { // Corregido
        val cursor = db.rawQuery("SELECT MAX(IDSala) FROM Salas", null)
        cursor.moveToFirst()
        val maxId = cursor.getLong(0)
        cursor.close()
        return maxId + 1
    }

    private fun generateUniqueConexion(db: SQLiteDatabase): Int {
        val cursor = db.rawQuery("SELECT MAX(IDconexion) FROM Conexiones", null)
        cursor.moveToFirst()
        val maxId = cursor.getInt(0)
        cursor.close()
        return maxId + 1
    }

    private fun generateUniquePaso(db: SQLiteDatabase): Int {
        val cursor = db.rawQuery("SELECT MAX(IDPaso) FROM Pasos", null)
        cursor.moveToFirst()
        val maxId = cursor.getInt(0)
        cursor.close()
        return maxId + 1
    }











    //Gets
    //Devuelve el ID asociado a un usuario
    @SuppressLint("Range")
    private fun getRowIdForUsuario(usuario: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            "Usuarios",
            null,
            "Usuario = ?",
            arrayOf(usuario),
            null,
            null,
            null,
            "1"
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    //Dado los datos de un edificio, comprueba si esta ya en el sistema
    fun getRowIdForEdificio(usuario: String, edificio: String, calle: String): Long {

        var db= writableDatabase
        val cursor = db.query(
            "Edificios",
            arrayOf("IDEdificio"),
            "Usuario = ? AND NombreE = ? AND Calle = ?",
            arrayOf(usuario, edificio, calle),
            null,
            null,
            null
        )
        var rowId = -1L

        if (cursor.moveToFirst()) {
            rowId = cursor.getLong(0)
            println(rowId)
        }
        cursor.close()
        return rowId
    }

    //Porporciona una lista de calle y edificios para cada usuario
    @SuppressLint("Range")
    fun getEdificiosYCallesPorUsuario(usuario: String): List<Triple<Long, String, String>> {
        val edificiosYCalles = mutableListOf<Triple<Long, String, String>>()
        readableDatabase.use { db ->
            val columns = arrayOf("IDEdificio", "NombreE", "Calle")
            val selection = "Usuario = ?"
            val selectionArgs = arrayOf(usuario)

            val cursor = db.query(
                "Edificios",
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                val idEdificio = cursor.getLong(cursor.getColumnIndex("IDEdificio"))
                val edificio = cursor.getString(cursor.getColumnIndex("NombreE"))
                val calle = cursor.getString(cursor.getColumnIndex("Calle"))
                edificiosYCalles.add(Triple(idEdificio, edificio, calle))
            }
            cursor.close()
        }
        return edificiosYCalles
    }


    @SuppressLint("Range")
    fun getSalas(IdEdificio: Int): List<Triple<String, Int, Pair<Int, Int>>> {
        val salas = mutableListOf<Triple<String, Int, Pair<Int, Int>>>()
        val db = readableDatabase
        val cursor = db.query(
            "Salas",
            arrayOf("IDSala", "NombreS", "CoordenadaX", "CoordenadaY"),
            "IDEdificio = ?",
            arrayOf(IdEdificio.toString()),
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            val idSala = cursor.getInt(cursor.getColumnIndex("IDSala"))
            val nombreSala = cursor.getString(cursor.getColumnIndex("NombreS"))
            val coordenadaX = cursor.getInt(cursor.getColumnIndex("CoordenadaX"))
            val coordenadaY = cursor.getInt(cursor.getColumnIndex("CoordenadaY"))
            val salaInfo = Triple(nombreSala, idSala, Pair(coordenadaX, coordenadaY))
            salas.add(salaInfo)
        }

        cursor.close()
        return salas
    }

    @SuppressLint("Range")
    fun getConexiones(IDSalaO: Int): List<Pair<Int, String>> {
        val conexiones = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val query = """
        SELECT Conexiones.IDconexion, Salas.NombreS
        FROM Conexiones
        INNER JOIN Salas ON Conexiones.IDSalaD = Salas.IDSala
        WHERE Conexiones.IDSalaO = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(IDSalaO.toString()))

        while (cursor.moveToNext()) {
            val idConexion = cursor.getInt(cursor.getColumnIndex("IDconexion"))
            val nombreSalaD = cursor.getString(cursor.getColumnIndex("NombreS"))
            conexiones.add(Pair(idConexion, nombreSalaD))
        }

        cursor.close()
        return conexiones
    }

    @SuppressLint("Range")
    private fun getIDConexion(Nombre:String, IdEdificio: String) : String? {
        val db = readableDatabase
        val query = """
        SELECT Salas.IDSala
        FROM Salas
        INNER JOIN Edificios ON Salas.IDEdificio = Edificios.IDEdificio
        WHERE Salas.NombreS = ? AND Edificios.IDEdificio = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(Nombre, IdEdificio))

        val idSala: String? = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex("IDSala"))
        } else {
            return "-1"
        }
        cursor.close()
        return idSala
    }

    @SuppressLint("Range")
    fun getPasos(IDConexion: Int): List<Pair<Int, String>> {
        val pasos = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val query = """
        SELECT IDPaso, Instruccion
        FROM Pasos
        WHERE IDconexion = ?
        ORDER BY Orden ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(IDConexion.toString()))

        while (cursor.moveToNext()) {
            val idPaso = cursor.getInt(cursor.getColumnIndex("IDPaso"))
            val instruccion = cursor.getString(cursor.getColumnIndex("Instruccion"))
            pasos.add(Pair(idPaso, instruccion))
        }

        cursor.close()
        return pasos
    }

    data class Edificio(
        val idEdificio: Long,
        val usuario: String,
        val nombreE: String,
        val calle: String
    )

    @SuppressLint("Range")
    fun getEdificiosFiltrados(usuarioID: String): List<Edificio> {
        val db = readableDatabase
        val userCursor = db.query(
            "Usuarios",
            arrayOf("dc", "mr"),
            "Usuario = ?",
            arrayOf(usuarioID),
            null,
            null,
            null
        )

        var dcValue = 0
        var mrValue = 0

        if (userCursor.moveToFirst()) {
            dcValue = userCursor.getInt(userCursor.getColumnIndex("dc"))
            mrValue = userCursor.getInt(userCursor.getColumnIndex("mr"))
        }

        userCursor.close()

        val resultEdificios = mutableListOf<Edificio>()

        val condition = if (dcValue == 1 && mrValue == 1) {
            "" // Ambos campos marcados, no aplicamos ninguna condición
        } else if (dcValue == 1) {
            "dc = 1"
        } else {
            "mr = 1"
        }

        val query = """
        SELECT Edificios.IDEdificio, Edificios.Usuario, Edificios.NombreE, Edificios.Calle
        FROM Edificios
        WHERE Edificios.Usuario IN (
            SELECT Usuario
            FROM Usuarios
            WHERE $condition
        )
    """.trimIndent()

        val conditionCursor = db.rawQuery(query, null)

        while (conditionCursor.moveToNext()) {
            val idEdificio = conditionCursor.getLong(conditionCursor.getColumnIndex("IDEdificio"))
            val usuario = conditionCursor.getString(conditionCursor.getColumnIndex("Usuario"))
            val nombreE = conditionCursor.getString(conditionCursor.getColumnIndex("NombreE"))
            val calle = conditionCursor.getString(conditionCursor.getColumnIndex("Calle"))
            resultEdificios.add(Edificio(idEdificio, usuario, nombreE, calle))
        }

        conditionCursor.close()

        return resultEdificios.distinctBy { it.idEdificio } // Eliminar duplicados por IDEdificio
    }

    @SuppressLint("Range")
    fun buscarIdSalaPorNombreYEdificio(nombreSala: String, idEdificio: Long): Long {

        val db = readableDatabase
        val selection = "nombreS = ? AND IDEdificio = ?"
        val selectionArgs = arrayOf(nombreSala, idEdificio.toString())

        val cursor = db.query(
            "Salas",
            arrayOf("IDSala"),
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var idSala: Long = -1 // Valor por defecto si no se encuentra ninguna coincidencia

        if (cursor.moveToFirst()) {
            idSala = cursor.getLong(cursor.getColumnIndex("IDSala"))
        }

        cursor.close()
        return idSala
    }

    @SuppressLint("Range")
    fun getSalasPorEdificio(idEdificio: Long): List<Int> {
        val salasList = mutableListOf<Int>()
        val db = readableDatabase
        val query = """
        SELECT *
        FROM Salas
        WHERE IDEdificio = ?
    """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(idEdificio.toString()))
        while (cursor.moveToNext()) {
            val idSala = cursor.getInt(cursor.getColumnIndex("IDSala"))

            salasList.add(idSala)
        }
        cursor.close()
        return salasList
    }


    @SuppressLint("Range")
    fun getConexionesUnicas(Lista : List<Int>, salaO: String, salaD:String): List<Int> {
        var no_limpio= true
        var listaN= Lista.toMutableList()

        //Elimina Salas con solo salidas o llegadas
        while(no_limpio){
            var salasDeSalida = mutableListOf<Int>()
            var salasDeLlegada = mutableListOf<Int>()

            val db = readableDatabase
            // Crear una consulta para buscar conexiones donde IDSalaO o IDSalaD estén en la lista idSalas
            val query = """
            SELECT DISTINCT IDSalaO, IDSalaD
            FROM Conexiones
            WHERE IDSalaO IN (${listaN.joinToString()})
               OR IDSalaD IN (${listaN.joinToString()})
            """.trimIndent()

            val cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val idSalaO = cursor.getInt(cursor.getColumnIndex("IDSalaO"))
                val idSalaD = cursor.getInt(cursor.getColumnIndex("IDSalaD"))

                if (listaN.contains(idSalaO)) {
                    salasDeSalida.add(idSalaO)
                    salasDeLlegada.add(idSalaD)
                } else {
                    salasDeSalida.add(idSalaD)
                    salasDeLlegada.add(idSalaO)
                }
            }
            cursor.close()

            if((salaD.toInt() in salasDeLlegada) && (salaO.toInt() in salasDeSalida)){
                salasDeSalida.add(salaD.toInt())
                salasDeLlegada.add(salaO.toInt())
            }else{
                return emptyList()
            }

            val diferenciaL = salasDeLlegada.subtract(salasDeSalida.toList().distinct().toSet()).toMutableList()
            val diferenciaS = salasDeSalida.subtract(salasDeLlegada.toList().distinct().toSet()).toMutableList()
            if (diferenciaL.isNotEmpty() && diferenciaS.isNotEmpty()) {
                listaN = listaN.filter { sala -> sala !in diferenciaL }.toMutableList()
                listaN = listaN.filter { sala -> sala !in diferenciaS }.toMutableList()
            }else{
                no_limpio=false
            }
        }
        return listaN
    }


    @SuppressLint("Range")
    fun crearTablaConexiones(Lista : List<Int>): List<Conexion> {
        val conexiones = mutableListOf<Conexion>()
        val db = readableDatabase

        // Crear una consulta para buscar conexiones donde IDSalaO o IDSalaD estén en la lista idSalas
        val query = """
        SELECT Conexiones.IDconexion, Conexiones.IDSalaO, Conexiones.IDSalaD, SalasD.CoordenadaX, SalasD.CoordenadaY
        FROM Conexiones
        INNER JOIN Salas AS SalasD ON Conexiones.IDSalaD = SalasD.IDSala
        WHERE Conexiones.IDSalaO IN (${Lista.joinToString()})
           OR Conexiones.IDSalaD IN (${Lista.joinToString()})
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val idConexion = cursor.getInt(cursor.getColumnIndex("IDconexion"))
            val idSalaO = cursor.getInt(cursor.getColumnIndex("IDSalaO"))
            val idSalaD = cursor.getInt(cursor.getColumnIndex("IDSalaD"))
            val coordenadaX = cursor.getInt(cursor.getColumnIndex("CoordenadaX"))
            val coordenadaY = cursor.getInt(cursor.getColumnIndex("CoordenadaY"))

            val conexion = Conexion(idConexion, idSalaO, idSalaD, coordenadaX, coordenadaY)
            conexiones.add(conexion)
        }
        cursor.close()
        return conexiones
    }

    fun contarConexiones(idsSalas: List<Int>, conexiones: List<Conexion>): Pair<List<Pair<Int, Int>>, Int> {
        val conteoSalas = mutableMapOf<Int, Int>()
        var salasCon3oMasConexiones = 0

        for (conexion in conexiones) {
            val salaO = conexion.salaO
            // Actualiza el conteo de salas
            conteoSalas[salaO] = (conteoSalas[salaO] ?: 0) + 1

            // Verifica si la sala tiene 3 o más conexiones
            if (conteoSalas[salaO] == 3) {
                salasCon3oMasConexiones++
            }
        }
        // Crea una lista de pares (ID de sala, número de conexiones)
        val resultados = idsSalas.map { idSala ->
            Pair(idSala, conteoSalas[idSala] ?: 0)
        }

        return Pair(resultados, ceil(salasCon3oMasConexiones.toDouble()/3).toInt())
    }

    private fun buscarCamino(salaO: Int, salaD: Int, conexiones: List<Conexion>, contador:Int, lista_conexiones:List<Pair<Int, Int>>) : List<Int>{

        val conexionSalaD = conexiones.find { it.salaD == salaD }
        val coordenadaXsalaD = conexionSalaD?.coordenadaX
        val coordenadaYsalaD = conexionSalaD?.coordenadaY
        var nuevoContador=contador
        var conexionesNuevo= conexiones
        var lista = emptyList<Int>()
        var idConexion= 0

        val conexionesSalaO = conexiones.filter { it.salaO == salaO }

        println("salaO")
        println(salaO)
        println("salaD")
        println(salaD)
        println("Conexiones")
        println(conexiones)


        while(lista.isEmpty()){
            val conexionCercana = conexionesSalaO.minByOrNull { nuevas_Conexiones ->
                val diferenciaX = abs(nuevas_Conexiones.coordenadaX - coordenadaXsalaD!!)
                val diferenciaY = abs(nuevas_Conexiones.coordenadaY - coordenadaYsalaD!!)
                diferenciaX + diferenciaY
            }


            if (conexionCercana != null) {
                idConexion= conexionCercana.idConexion
                if(conexionCercana.salaD==salaD){
                    return listOf(idConexion)
                }else{
                    val conexionEncontrada = lista_conexiones.find { par ->
                        par.first == conexionCercana?.salaD
                    }

                    if(conexionEncontrada?.second ?:1 >=3){
                        nuevoContador=contador-1
                    }else{
                        if(contador<0){

                            return lista
                        }
                    }
                    conexionesNuevo = conexionesNuevo.filter { conexion -> conexion.idConexion != conexionCercana.idConexion }
                    conexionesNuevo = conexionesNuevo.filter { conexion -> conexion.salaO != salaD || conexion.salaD != salaO }

                    lista = buscarCamino(conexionCercana.salaD,salaD,conexionesNuevo, nuevoContador, lista_conexiones)
                }
            }else{
                println("NO hay conexiones cercanas RETORNAMOS")
                println(lista)
                return lista
            }
        }
        println("Encontramos camino, retornamos")
        println(lista)
        return listOf(idConexion) + lista
    }

    @SuppressLint("Range")
    fun conseguirInstrucciones(camino: List<Int>): String {
        val db = readableDatabase
        val instrucciones = mutableListOf<String>()

        for (conexionId in camino) {
            val cursor = db.query(
                "pasos",
                arrayOf("Instruccion"),
                "IDconexion = ?",
                arrayOf(conexionId.toString()),
                null,
                null,
                "orden"
            )

            while (cursor.moveToNext()) {
                val instruccion = cursor.getString(cursor.getColumnIndex("Instruccion"))
                instrucciones.add(instruccion)
            }

            cursor.close()

            val nombreSalaD = obtenerNombreSalaD(conexionId.toString())

            instrucciones.add("Has alcanzado la sala $nombreSalaD")
        }

        return instrucciones.joinToString("\n") // Devolver las instrucciones como un solo String separado por saltos de línea
    }


    @SuppressLint("Range")
    private fun obtenerNombreSalaD(conexionId: String): String {
        var IdSala= 0
        val db = readableDatabase
        val cursor = db.query(
            "Conexiones",
            arrayOf("IDSalaD"),
            "IDConexion = ?",
            arrayOf(conexionId),
            null,
            null,
            null
        )

        IdSala =if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex("IDSalaD")).toInt()
        } else {
            1
        }
        cursor.close()


        val cursor2 = db.query(
            "Salas",
            arrayOf("NombreS"),
            "IDSala = ?",
            arrayOf(IdSala.toString()),
            null,
            null,
            null
        )

        if (cursor2.moveToFirst()) {
            return cursor2.getString(cursor2.getColumnIndex("NombreS"))
        } else {
            return ""
        }


    }


    fun generarCamino(idEdificio: String, salaO: String, salaD:String): DataCamino {
        var listaSalas =  getSalasPorEdificio(idEdificio.toLong())

        listaSalas = getConexionesUnicas(listaSalas, salaO, salaD)

        if(listaSalas.isEmpty()){
            return DataCamino(
                salaO,
                salaD,
                "No se encontro ningun camino",
                emptyList(), // Lista de conexiones vacía
                emptyList(), // Lista de conexiones vacía
                0, // Valor de caminoMax por defecto
                0
            )
        }
        var conexiones: List<Conexion>  = crearTablaConexiones(listaSalas)
        var contador = contarConexiones(listaSalas,conexiones)
        var listaConexiones= contador.first
        var caminoMax= contador.second
        var camino = buscarCamino(salaO.toInt(),salaD.toInt(),conexiones,caminoMax,listaConexiones)
        return if(camino.isNotEmpty()){
            DataCamino (salaO,salaD,conseguirInstrucciones(camino),conexiones,listaConexiones,caminoMax, camino.first())
        }else{
            DataCamino (salaO,salaD,"No existe un camino entre estas dos salas",conexiones,listaConexiones,caminoMax, 0)
        }

    }

    fun nuevoCamino(camino: DataCamino): DataCamino{

        camino.conexiones= camino.conexiones.filter { conexion ->
            conexion.idConexion != camino.eliminar
        }

        println("Entrando a buscador")
        var caminoN = buscarCamino(camino.salaO.toInt(),camino.salaD.toInt(),camino.conexiones,camino.caminoMax, camino.ListaConexiones)

        return if(caminoN.isEmpty()){
            DataCamino (camino.salaO,camino.salaD,"No existe un camino entre estas dos salas",camino.conexiones,camino.ListaConexiones,camino.caminoMax, 0)
        }else{
            DataCamino (camino.salaO,camino.salaD,conseguirInstrucciones(caminoN),camino.conexiones,camino.ListaConexiones,camino.caminoMax, caminoN.first())
        }


    }














    //Eliminar entradas
    fun eliminarEdificios(idEdificio: Long): Long { // Corregido
        val db = writableDatabase
        return db.delete(
            "Edificios",
            "IDEdificio = ?",
            arrayOf(idEdificio.toString())
        ).toLong()
    }

    fun eliminarSala(idSala: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("Salas", "IDSala = ?", arrayOf(idSala.toString()))
        return rowsDeleted > 0
    }

    fun eliminarConexion(idConexion: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("Conexiones", "IDconexion = ?", arrayOf(idConexion.toString()))
        return rowsDeleted > 0
    }

    fun eliminarPaso(idPaso: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("Pasos", "IDPaso = ?", arrayOf(idPaso.toString()))
        return rowsDeleted > 0
    }



}