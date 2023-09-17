package com.shikaiji.guiadointeriores20

data class DataCamino (var salaO: String,
                       var salaD: String,
                       var instrucciones: String,
                       var conexiones: List<Conexion>,
                       var ListaConexiones: List<Pair<Int,Int>>,
                       var caminoMax: Int,
                       var eliminar: Int)