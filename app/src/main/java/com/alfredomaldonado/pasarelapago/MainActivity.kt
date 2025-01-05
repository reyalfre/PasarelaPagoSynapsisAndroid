package com.alfredomaldonado.pasarelapago

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.synap.pay.SynapPayButton
import com.synap.pay.handler.EventHandler
import com.synap.pay.handler.payment.SynapAuthorizeHandler
import com.synap.pay.model.payment.SynapAddress
import com.synap.pay.model.payment.SynapCardStorage
import com.synap.pay.model.payment.SynapCountry
import com.synap.pay.model.payment.SynapCurrency
import com.synap.pay.model.payment.SynapDocument
import com.synap.pay.model.payment.SynapFeatures
import com.synap.pay.model.payment.SynapMetadata
import com.synap.pay.model.payment.SynapOrder
import com.synap.pay.model.payment.SynapPerson
import com.synap.pay.model.payment.SynapProduct
import com.synap.pay.model.payment.SynapSettings
import com.synap.pay.model.payment.SynapTransaction
import com.synap.pay.model.payment.response.SynapAuthorizeResponse
import com.synap.pay.model.security.SynapAuthenticator
import com.synap.pay.theming.SynapLightTheme
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    private lateinit var paymentWidget: SynapPayButton
    private lateinit var synapForm: FrameLayout
    private lateinit var synapButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        synapForm = findViewById(R.id.synapForm)
        synapForm.visibility = View.GONE

        synapButton = findViewById(R.id.synapButton)
        synapButton.visibility = View.GONE
        synapButton.setOnClickListener { paymentWidget.pay() }


        val startPayment = findViewById<Button>(R.id.startPaymentButton)
        startPayment.setOnClickListener { startPayment() }


    }


    private fun startPayment() {
        // Muestre el contenedor del formulario de pago
        synapForm.visibility = View.VISIBLE

        // Muestre el botón de pago
        synapButton.visibility = View.VISIBLE

        // Crea el objeto del widget de pago
        paymentWidget = SynapPayButton.create(synapForm)

        // Tema de fondo en la tarjeta (Light o Dark)
        val theme = SynapLightTheme() // Fondo de tarjeta claro
        // val theme = SynapDarkTheme() // Fondo de tarjeta oscuro
        SynapPayButton.setTheme(theme)

        // Seteo del ambiente ".SANDBOX" o ".PRODUCTION"
        SynapPayButton.setEnvironment(SynapPayButton.Environment.SANDBOX)

        // Seteo de los campos de transacción
        val transaction: SynapTransaction = this.buildTransaction()

        // Seteo de los campos de autenticación de seguridad
        val authenticator: SynapAuthenticator = this.buildAuthenticator(transaction)

        // Control de eventos en el formulario de pago
        SynapPayButton.setListener(object : EventHandler {
            //  val paymentButton: Button = findViewById(R.id.synapButton)

            override fun onEvent(event: SynapPayButton.Events?) {
                val paymentButton: Button
                when (event) {
                    SynapPayButton.Events.START_PAY -> {
                        paymentButton = findViewById(R.id.synapButton)
                        paymentButton.visibility = View.GONE
                    }

                    SynapPayButton.Events.INVALID_CARD_FORM -> {
                        paymentButton = findViewById(R.id.synapButton)
                        paymentButton.visibility = View.VISIBLE
                    }

                    SynapPayButton.Events.VALID_CARD_FORM -> {
                        // No hacer nada
                    }

                    SynapPayButton.Events.CARDSTORAGE_LOADED -> TODO()
                    null -> TODO()
                }
            }
        })

        paymentWidget.configure(
            authenticator,
            transaction,
            //  synapWebView, // Seteo de WebView de autenticación 3DS

            // Manejo de la respuesta
            object : SynapAuthorizeHandler {
                override fun success(response: SynapAuthorizeResponse) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare()
                    }
                    val resultSuccess = response.success
                    if (resultSuccess) {
                        val resultAccepted = response.result.accepted
                        val resultMessage = response.result.message
                        if (resultAccepted) {
                            // Agregue el código según la experiencia del cliente para la autorización
                            showMessage(resultMessage)
                        } else {
                            // Agregue el código según la experiencia del cliente para la denegación
                            showMessage(resultMessage)
                        }
                    } else {
                        val messageText = response.message.text
                        // Agregue el código de la experiencia que desee visualizar en un error
                        showMessage(messageText)
                    }
                    Looper.loop()
                }

                override fun failed(response: SynapAuthorizeResponse) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare()
                    }
                    val messageText = response.message.text
                    // Agregue el código de la experiencia que desee visualizar en un error
                    showMessage(messageText)
                    Looper.loop()
                }
            }
        )

        val paymentButton: Button = findViewById(R.id.synapButton)
        paymentButton.visibility = View.VISIBLE
    }


    private fun buildTransaction(): SynapTransaction {
        // Genere el número de orden, este es solo un ejemplo
        val number = System.currentTimeMillis().toString()

        // Seteo de los datos de transacción
        // Referencie al objeto país
        val country = SynapCountry()
        // Seteo del código de país
        country.code = "PER" // Código de País (ISO 3166-2)


        // Referencie al objeto moneda
        val currency = SynapCurrency()
        // Seteo del código de moneda
        currency.code = "PEN" // Código de Moneda - Alphabetic code (ISO 4217)


        // Seteo del monto
        val amount = "1.00"

        // Referencie al objeto cliente
        val customer = SynapPerson()
        // Seteo del cliente
        customer.name = "Javier"
        customer.lastName = "Pérez"

        // Referencie al objeto dirección del cliente
        val address = SynapAddress()
        // Seteo del pais (country), niveles de ubicación geográfica (levels), dirección (line1 y line2) y código postal (zip)
        address.country = "PER" // Código de País del cliente (ISO 3166-2)
        address.levels = mutableListOf("150000", "150100", "150101") // Código de Área (Ubigeo)
        address.line1 = "Av La Solidaridad 103" // Dirección
        address.zip = "15034"
        customer.address = address


        // Seteo del email y teléfono
        customer.email = "javier.perez@synapsis.pe"
        customer.phone = "999888777"

        // Referencie al objeto documento del cliente
        val document = SynapDocument()
        // Seteo del tipo y número de documento
        document.type = "DNI" // [DNI, CE, PAS, RUC]
        document.number = "44556677"
        customer.document = document


        // Seteo de los datos de envío y facturación
        val shipping = customer // Opcional, misma estructura que "customer"
        val billing = customer // Opcional, misma estructura que "customer"

        // Referencie al objeto producto
        val productItem = SynapProduct()
        // Seteo de los datos de producto
        productItem.code = "123" // Opcional
        productItem.name = "Llavero"
        productItem.quantity = "1" // Opcional
        productItem.unitAmount = "1.00" // Opcional
        productItem.amount = "1.00" // Opcional


        // Referencie al objeto lista de producto
        // val products = mutableListOf(productItem)
        val products: MutableList<SynapProduct> = ArrayList()
        products.add(productItem)

        // Referencie al objeto metadata - Opcional
        val metadataItem = SynapMetadata()
        // Seteo de los datos de metadata
        metadataItem.name = "name1"
        metadataItem.value = "value1"


        // Referencie al objeto lista de metadata - Opcional
        // val metadataList = mutableListOf(metadataItem)

        val metadataList: MutableList<SynapMetadata> = ArrayList()
        metadataList.add(metadataItem)

        // Referencie al objeto orden
        val order = SynapOrder()
        // Seteo de los datos de orden
        order.number = number
        order.amount = amount
        order.country = country
        order.currency = currency
        order.products = products
        order.customer = customer
        order.shipping = shipping // Opcional
        order.billing = billing // Opcional
        order.metadata = metadataList // Opcional


        // Referencie al objeto configuración
        val settings = SynapSettings()
        // Seteo de los datos de configuración
        settings.brands = listOf("VISA", "MSCD", "AMEX", "DINC")
        settings.language = "es_PE"
        settings.businessService = "MOB"

        val transaction = SynapTransaction()
        transaction.order = order
        transaction.settings = settings

        // Referencia al objeto features (Recordar Tarjeta) - Opcional
        val cardStorage = SynapCardStorage()
        cardStorage.userIdentifier =
            "javier.perez@synapsis.pe" // Identificador definido por el comercio

        val features = SynapFeatures()
        features.cardStorage = cardStorage

        transaction.features = features
        return transaction


        /**
        // Referencie al objeto transacción
        return SynapTransaction().apply
        {
        // Seteo de los datos de transacción
        this.order = order
        this.features = features
        this.settings = settings
        }
         **/
    }

    private fun buildAuthenticator(transaction: SynapTransaction): SynapAuthenticator {
        val apiKey = "ab254a10-ddc2-4d84-8f31-d3fab9d49520"

        // La signatureKey y la función de generación de firma debe usarse e implementarse en el servidor del comercio utilizando la función criptográfica SHA-512
        // Solo con propósito de demostrar la funcionalidad, se implementará en el ejemplo
        // (Bajo ninguna circunstancia debe exponerse la signatureKey y la función de firma desde la aplicación porque compromete la seguridad)
        val signatureKey = "eDpehY%YPYgsoludCSZhu*WLdmKBWfAo"

        val signature = generateSignature(transaction, apiKey, signatureKey)

        // El campo onBehalf es opcional y se usa cuando un comercio agrupa otros sub comercios
        // La conexión con cada sub comercio se realiza con las credenciales del comercio agrupador
        // y enviando el identificador del sub comercio en el campo onBehalf
        // val onBehalf = "cf747220-b471-4439-9130-d086d4ca83d4"

        // Referencie el objeto de autenticación
        return SynapAuthenticator().apply {
            // Seteo de identificador del comercio (apiKey)
            identifier = apiKey

            // Seteo de firma, que permite verificar la integridad de la transacción
            this.signature = signature

            // Seteo de identificador de sub comercio (solo si es un subcomercio)
            // this.onBehalf = onBehalf
        }
    }
//Siguiente parte

    // Muestra el mensaje de respuesta
    private fun showMessage(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(true)
            .setPositiveButton("OK") { dialog, _ ->
                // Finaliza el intento de pago y regresa al inicio, el comercio define la experiencia del cliente
                val looper = Handler(applicationContext.mainLooper)
                looper.post {
                    synapForm.visibility = View.GONE
                    synapButton.visibility = View.GONE
                }
                dialog.cancel()
            }

        val alert = builder.create()
        alert.show()
    }

    // La signatureKey y la función de generación de firma debe usarse e implementarse en el servidor del comercio utilizando la función criptográfica SHA-512
// solo con propósito de demostrar la funcionalidad, se implementará en el ejemplo
// (bajo ninguna circunstancia debe exponerse la signatureKey y la función de firma desde la aplicación porque compromete la seguridad)
    private fun generateSignature(
        transaction: SynapTransaction,
        apiKey: String,
        signatureKey: String
    ): String {
        val orderNumber = transaction.order.number
        val currencyCode = transaction.order.currency.code
        val amount = transaction.order.amount

        val rawSignature = apiKey + orderNumber + currencyCode + amount + signatureKey
        return sha512Hex(rawSignature)
    }

    private fun sha512Hex(value: String): String {
        val sb = StringBuilder()
        try {
            val md = MessageDigest.getInstance("SHA-512")
            val bytes = md.digest(value.toByteArray(Charsets.UTF_8))
            for (byte in bytes) {
                sb.append(((byte.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }


}