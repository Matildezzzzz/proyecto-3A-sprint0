<?php
// ---------------------------------------------------------------------
// Fichero:     AlmacenMediciones.php
// Descripción: Capa de acceso a datos. Guarda las medidas en la tabla
//              "medidas" de la base de datos y devuelve la última.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------

class AlmacenMediciones
{
    private $bd;

    // --------------------------------------------------------------
    // bd: PDO --> AlmacenMediciones() -->
    // Los errores de la base de datos se lanzan como excepciones.
    // --------------------------------------------------------------
    public function __construct(PDO $bd)
    {
        $this->bd = $bd;
        $this->bd->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    }

    // --------------------------------------------------------------
    // medida: {tipo, valor, contador, fecha} --> insertar() --> N
    //
    // 1. INSERT en "medidas" (fecha convertida a DATETIME).
    // 2. Devolver el id que ha asignado la base de datos.
    // --------------------------------------------------------------
    public function insertar(array $medida)
    {
        $sentencia = $this->bd->prepare(
            'INSERT INTO medidas (tipo, valor, contador, fecha) VALUES (?, ?, ?, ?)'
        );

        $sentencia->execute([
            $medida['tipo'],
            $medida['valor'],
            $medida['contador'],
            date('Y-m-d H:i:s', strtotime($medida['fecha'])),
        ]);

        return (int) $this->bd->lastInsertId();
    }

    // --------------------------------------------------------------
    // {id, tipo, valor, contador, fecha} | null <-- ultima() <--
    //
    // 1. SELECT de la fila con el id más alto.
    // 2. Si no hay ninguna: devolver null.
    // 3. Devolver la fila con números enteros y la fecha en ISO 8601.
    // --------------------------------------------------------------
    public function ultima()
    {
        $fila = $this->bd
            ->query('SELECT id, tipo, valor, contador, fecha FROM medidas ORDER BY id DESC LIMIT 1')
            ->fetch(PDO::FETCH_ASSOC);

        if ($fila === false) {
            return null;
        }

        return [
            'id' => (int) $fila['id'],
            'tipo' => (int) $fila['tipo'],
            'valor' => (int) $fila['valor'],
            'contador' => (int) $fila['contador'],
            'fecha' => date('c', strtotime($fila['fecha'])),
        ];
    }
}