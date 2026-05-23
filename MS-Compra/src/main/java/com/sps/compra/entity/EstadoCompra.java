package com.sps.compra.entity;

public enum EstadoCompra {
    CREADA,
    EN_VALIDACION_SNS,
    APROBADA,        // SNS aprobo, correo enviado, esperando pago
    RECHAZADA,       // SNS rechazo
    PAGADA,          // SaludPay confirmo pago
    TERMINADA        // SAM y SHC notificados
}
