package com.shikaiji.guiadointeriores20
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


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
    private fun getRowIdForEdificio(usuario: String, edificio: String, calle: String): Long {
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
        val rowId = if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            -1
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