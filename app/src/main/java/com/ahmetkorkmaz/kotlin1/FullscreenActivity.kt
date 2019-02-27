package com.ahmetkorkmaz.kotlin1

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_fullscreen.*
//import kotlinx.android.synthetic.main.activity_fullscreen.degisken
import kotlinx.android.synthetic.main.cihaz_listesi.view.*
import kotlinx.android.synthetic.main.ileri.view.*
//import kotlinx.android.synthetic.main.sartolumluolumsuz.*
import kotlinx.android.synthetic.main.sart1.view.sart1Boolean
import kotlinx.android.synthetic.main.sart2.view.sart2Boolean
import kotlinx.android.synthetic.main.sart3.view.sart3Boolean
import kotlinx.android.synthetic.main.sart_engel.view.sartEngelBoolean
import kotlinx.android.synthetic.main.renk.view.renkDegisKare
import kotlinx.android.synthetic.main.sart_cizgi.view.sartCizgiBoolean
import kotlinx.android.synthetic.main.sart_cizgi.view.kareCizgi
import kotlinx.android.synthetic.main.sartdegtosayi.view.sartdegtosayiBoolean
import kotlinx.android.synthetic.main.sartdegtodeg.view.sartdegtodegBoolean
//import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Math.abs
import java.util.*
import kotlin.math.roundToInt


class FullscreenActivity : AppCompatActivity() {

    private var txtHareketClicked = false
    private var txtMatematikClicked = false
    private var x = 0F
    private var y = 0F
    private var siradakiNesneListesi=mutableSetOf<Short?>()
    private var txtKararClicked = false
    private var txtDonguClicked = false
    private var playing = false
    private var txtFonksiyonClicked = false
    lateinit var pairedDevicesArrayAdapter  :ArrayAdapter<String>
    lateinit var mNewDevicesArrayAdapter  :ArrayAdapter<String>
    lateinit var nesneListener : OnTouchListener
    private var txtDegiskenClicked = false
    private var nesneListesi = mutableSetOf<Nesne>()
    private var geciciViewListesi = mutableSetOf<Short>()
    private var geciciViewListesi2 = mutableSetOf<Short>()
    private var geciciViewListesi3 = mutableSetOf<Short>()
    private var kaydirilanlar = mutableSetOf<Short>()
    private var fonkListesi = arrayListOf<String>()
    var sound = true
    private val robotName = "HC-05"
    private val robotName2 = "HC-06"
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable
    private lateinit var mRunnable2:Runnable
    var mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var mBluetoothSocket: BluetoothSocket? = null
    lateinit var mBluetoothAdapter: BluetoothAdapter
    var mIsConnected: Boolean = false
    private var buffer = ByteArray(16)
    private var string=""
    lateinit var geciciNesne:Nesne
    companion object {
        var viewId = 0
        var difYBasla=0
        var adet:Short=1
        var elmaDeger:Short=0
        var armutDeger:Short=0
        var portakalDeger:Short=0
        var seftaliDeger:Short=0
        var muzDeger:Short=0
        var birinciSayi:Short=0
        var ikinciSayi:Short= 0
        var anaDeg:Short= 0
        var karsilastirmaOperatoru:Short=1
        var degiskenDeger:Short=0
        var birinciDeg:Short=0
        var ikinciDeg:Short=0
        var islemMat:Short=1
        var duzenlemeModu:Boolean=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.cihaz_listesi,R.id.textCihaz)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.cihaz_listesi,R.id.textCihaz)
        btListView.adapter = pairedDevicesArrayAdapter
        pairedDevicesArrayAdapter.notifyDataSetChanged()
        btListView2.adapter = mNewDevicesArrayAdapter
        mNewDevicesArrayAdapter.notifyDataSetChanged()
        btListView.onItemClickListener = mDeviceClickListener
        btListView2.onItemClickListener = mDeviceClickListener
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_desteklenmiyor, Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this, R.string.bluetooth_aciliyor, Toast.LENGTH_LONG).show()
            mBluetoothAdapter.enable()
            mBluetoothAdapter.startDiscovery()
            var pairedDevices = mBluetoothAdapter.bondedDevices
            if (pairedDevices.size > 0) {
                for (device: BluetoothDevice in pairedDevices) {
                    if(device.name.substring(0..4) == robotName||device.name.substring(0..4) == robotName2) {
                        pairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter)
        val clickSound = MediaPlayer.create(this, R.raw.click)
        val numberSound = MediaPlayer.create(this, R.raw.number)
        val trashSound = MediaPlayer.create(this, R.raw.trash)
        val factorySound = MediaPlayer.create(this, R.raw.factory)
        val open = MediaPlayer.create(this, R.raw.open)
        val inflater2 = LayoutInflater.from(this)
        val view2 = inflater2.inflate(R.layout.eylemgolge, frameLayout, false)
        pencil.y = floatingActionButton.y
        pencil.x = floatingActionButton.x
        mHandler=Handler()
        mRunnable= Runnable{
           // while(true) {
            textView6.text="En Son Id: "+viewId.toString()

            if (mBluetoothSocket != null) {
                    var byteCount = mBluetoothSocket!!.inputStream.available()
                    //twBluetoothGelenVeri.text = byteCount.toString()//string
                    if (byteCount > 0) {
                        //Toast.makeText(this, "gelen veri var ", Toast.LENGTH_SHORT).show()
                        try {
                            byteCount = mBluetoothSocket!!.inputStream.read(buffer)
                            string = String(buffer, 0, byteCount)
                            //Toast.makeText(this, "gelen veri: " +string, Toast.LENGTH_SHORT).show()
                            twBluetoothGelenVeri.text = string
                            textView.text="elma: "+elmaDeger.toString()
                            textView2.text="armut: "+armutDeger.toString()
                            textView3.text="portakal: "+portakalDeger.toString()
                            textView4.text="seftali: "+seftaliDeger.toString()
                            textView5.text="muz: "+muzDeger.toString()
                        } catch (e: IOException) {
                            Toast.makeText(this, R.string.okuma_hatasi, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            //}
            //mHandler.postDelayed(mRunnable, 200)
        }
        //mHandler.postDelayed(mRunnable, 2000)
        view2.layoutParams.height = btnFonksiyon.height
        frameLayout.addView(view2)
        view2.visibility = INVISIBLE
        val inflater3 = LayoutInflater.from(this)
        val view3 = inflater3.inflate(R.layout.sartgolge, frameLayout, false)
        view3.layoutParams.height = btnFonksiyon.height
        frameLayout.addView(view3)
        view3.visibility = INVISIBLE
        pencereBtBaglanti.visibility = INVISIBLE
        nesneListener = OnTouchListener { Nesne: View, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                //sabitViewListesiYap()
                geciciViewListesiYap(Nesne.id.toShort())
                x = Nesne.x
                y = Nesne.y
                var ekleDifY = enAlttakiCocukBottom(Nesne.id.toShort())-Nesne.y-view2.height/2
                var benCikincaBosmuKalacak=0
                for(i in nesneListesi){
                    if(i.nesneIcId==Nesne.id.toShort()){
                        benCikincaBosmuKalacak=2
                        break
                    }
                    else{
                        benCikincaBosmuKalacak=1
                    }
                }
                kisaltilacakNesneleriKisalt(Nesne.id.toShort(),ekleDifY.toInt(),view2.height/2, benCikincaBosmuKalacak)
                geciciViewListesi3.clear()
                kop(Nesne.id.toShort())
                Nesne.bringToFront()
                tumCocuklarinIsiklariniSariYap(Nesne.id.toShort())
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                //trash.visibility = VISIBLE
                view2.visibility = INVISIBLE
                Nesne.y = motionEvent.rawY - ileritek.height*0.75.toFloat()
                Nesne.x = motionEvent.rawX - ileritek.width * 0.5.toFloat()
                var difX = Nesne.x - x
                var difY = Nesne.y - y
                x=Nesne.x
                y=Nesne.y
                tumCocuklariTasi(difX,difY)
                for (HerbirNesne: Nesne in nesneListesi) {
                    if (HerbirNesne.yapisibil && HerbirNesne.nesneTuru!=2.toShort() && HerbirNesne.nesneTuru!=4.toShort()){
                        if (HerbirNesne.nesneTuru==1.toShort()||HerbirNesne.nesneTuru==3.toShort()) {
                            if (HerbirNesne.nesneResim.y + ileritek.height * 0.2 < Nesne.y && HerbirNesne.nesneResim.y + ileritek.height * 1.3 > Nesne.y) //yakınlarda başa nesne varsa
                            {
                                if (abs(HerbirNesne.nesneResim.x - Nesne.x) < HerbirNesne.nesneResim.width) {
                                    view2.x = HerbirNesne.nesneResim.x + ileritek.width * 0.165.toFloat()
                                    view2.y = HerbirNesne.nesneResim.y + ileritek.height * 0.85.toFloat()
                                    view2.visibility = VISIBLE
                                    view2.bringToFront()
                                }
                            }
                        }
                        if (HerbirNesne.nesneResim.y + (HerbirNesne.nesneResim.height - ileritek.height * 0.2) < Nesne.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height + (ileritek.height * 0.25) > Nesne.y)//yakınlarda başa nesne varsa
                        {
                            if (abs(HerbirNesne.nesneResim.x - Nesne.x) < HerbirNesne.nesneResim.width) {
                                //yerleştirme
                                view2.x = HerbirNesne.nesneResim.x
                                view2.y = HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height - (view2.height/2).toFloat()
                                view2.visibility = VISIBLE
                                view2.bringToFront()
                            }
                        }

                    }
                }
                if (Nesne.x >= trash.x - Nesne.width) {
                    if (Nesne.y >= trash.y - Nesne.height)
                    trash.setImageResource(R.drawable.blockly_trash_open)
                    else{
                        trash.setImageResource(R.drawable.blockly_trash)
                    }
                } else {
                    trash.setImageResource(R.drawable.blockly_trash)
                }
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {//elinikaldırdığımda
                view2.visibility = INVISIBLE
                //trash.visibility = INVISIBLE
                geciciViewListesi3.clear()
                kaydirilanlar.clear()
                var yapistimi=false
                if (Nesne.x >= trash.x - Nesne.width && Nesne.y >= trash.y - Nesne.height) {//sil
                    yokol(Nesne.id.toShort())
                    trashSound.start()
                    trash.setImageResource(R.drawable.blockly_trash)
                }//silme
                //fonksiyon
                else if (Nesne.x >= factory.x - ileritek.width && Nesne.y >= factory.y - ileritek.height&&Nesne.y <= factory.y + ileritek.height) {//sil
                   /* if (fonkListesi.size>6) {
                        Toast.makeText(this,"Sistem Performansı Açısından Daha Fazla Fonksiyona İzin Verilmiyor!",Toast.LENGTH_LONG).show()
                    }
                    else{*/
                    btFonkTamam.setOnClickListener {
                        if(etFonksiyon.text.toString().length<1) {
                            Toast.makeText(this@FullscreenActivity, getString(R.string.fonk_ad_giriniz), Toast.LENGTH_SHORT).show()
                        }

                        else if (fonkListesi.contains(etFonksiyon.text.toString())) {
                            Toast.makeText(this,"Aynı İsimde Bir Fonksiyon Var Lütfen Farklı Bir İsim Veriniz!",Toast.LENGTH_LONG).show()
                        }
                        else{
                            pencereFonksiyonOlustur.visibility=View.INVISIBLE
                            fonksiyonOlustur(Nesne.x, Nesne.y, Nesne.id.toShort(), etFonksiyon.text.toString())
                            etFonksiyon.text.clear()
                            factory.setImageResource(R.drawable.factory)
                            donus(Nesne.id.toShort())
                            factorySound.start()
                        }

                    }

                    btFonkIptal.setOnClickListener {
                        pencereFonksiyonOlustur.visibility=View.INVISIBLE
                    }
                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                    pencereKacDefa.visibility=View.INVISIBLE
                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                    pencereFonksiyonOlustur.visibility=View.VISIBLE
                    pencereBtBaglanti.visibility=View.INVISIBLE

               // }
                }

                else {
                    tumCocuklariPasifYap(Nesne.id.toShort())

                    for (HerbirNesne: Nesne in nesneListesi) {
                        if (HerbirNesne.yapisibil && HerbirNesne.nesneTuru!=2.toShort()  && HerbirNesne.nesneTuru!=4.toShort()) {
                            if (HerbirNesne.nesneTuru==1.toShort()||HerbirNesne.nesneTuru==3.toShort())
                            {
                                if (HerbirNesne.nesneResim.y + ileritek.height *0.2 < Nesne.y && HerbirNesne.nesneResim.y + ileritek.height *1.3 > Nesne.y)//yakınlarda başa nesne varsa
                                {
                                    if (abs(HerbirNesne.nesneResim.x - Nesne.x) < HerbirNesne.nesneResim.width) {
                                        //yerleştirme
                                        yapistimi=true
                                        x=Nesne.x
                                        y=Nesne.y
                                        Nesne.x = HerbirNesne.nesneResim.x+ileritek.width*0.165.toFloat()
                                        Nesne.y = HerbirNesne.nesneResim.y + ileritek.height*0.85.toFloat()
                                        var difX = Nesne.x - x
                                        var difY = Nesne.y - y
                                        tumCocuklariTasi(difX,difY)
                                        //tumCocuklariTasi(difX,difY)
                                        //geciciViewListesi2.clear()
                                        asagiKaydirilacaklariSec(HerbirNesne.nesneIcId)
                                        var ekleDifY = enAlttakiCocukBottom(Nesne.id.toShort())-Nesne.y-view2.height/2
                                        var iciBosMu=0
                                        for(i in nesneListesi){
                                            if(i.nesneId==HerbirNesne.nesneId){
                                                if(i.nesneIcId==null){
                                                    iciBosMu=2
                                                }
                                                else{
                                                    iciBosMu=1
                                                }
                                            }
                                        }
                                        eklendigimYerdekileriBenimAltimaEkle(ekleDifY)
                                        eklendigimYerdekiCocugunParentiniBenYap(HerbirNesne.nesneIcId,enAlttakiCocukBul(Nesne.id.toShort()))
                                        icYapis(Nesne.id.toShort(), HerbirNesne.nesneId)
                                        //geciciViewListesi3.clear()
                                        uzatilacakNesneleriUzat(Nesne.id.toShort(),ekleDifY.toInt(),view2.height/2,iciBosMu)
                                        clickSound.start()
                                        Nesne.isClickable = false
                                        object : CountDownTimer(500, 50) {
                                            override fun onFinish() {
                                                Toast.makeText(this@FullscreenActivity,"süre doldu",Toast.LENGTH_SHORT)
                                                Nesne.isClickable = true
                                            }
                                            override fun onTick(millisUntilFinished: Long) {

                                            }
                                        }.start()
                                    }
                                }
                            }//içe yapışma sorun yok
                            if (HerbirNesne.nesneResim.y + (HerbirNesne.nesneResim.height - ileritek.height * 0.2) < Nesne.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height + (ileritek.height * 0.25) > Nesne.y)//yakınlarda başa nesne varsa
                            {
                                if (abs(HerbirNesne.nesneResim.x - Nesne.x) < HerbirNesne.nesneResim.width) {
                                    //yerleştirme
                                    yapistimi=true
                                    x=Nesne.x
                                    y=Nesne.y
                                    Nesne.x = HerbirNesne.nesneResim.x
                                    Nesne.y = HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height - (view2.height/2).toFloat()
                                    var difX = Nesne.x - x
                                    var difY = Nesne.y - y
                                    tumCocuklariTasi(difX,difY)
                                    asagiKaydirilacaklariSec(HerbirNesne.nesneAltId)
                                    var ekleDifY = enAlttakiCocukBottom(Nesne.id.toShort())-Nesne.y-view2.height/2
                                    eklendigimYerdekileriBenimAltimaEkle(ekleDifY)
                                    eklendigimYerdekiCocugunParentiniBenYap(HerbirNesne.nesneAltId,enAlttakiCocukBul(Nesne.id.toShort()))
                                    yapis(Nesne.id.toShort(), HerbirNesne.nesneId )
                                    //geciciViewListesi3.clear()
                                    uzatilacakNesneleriUzat(Nesne.id.toShort(),ekleDifY.toInt(),view2.height/2,1)
                                    clickSound.start()
                                    Nesne.isClickable = false
                                    object : CountDownTimer(500, 50) {
                                        override fun onFinish() {
                                            Toast.makeText(this@FullscreenActivity,"süre doldu",Toast.LENGTH_SHORT)
                                            Nesne.isClickable = true
                                        }
                                        override fun onTick(millisUntilFinished: Long) {
                                        }
                                    }.start()
                                }
                            }//alta yapışma

                        }
                    }
                    //if(!yapistimi)
                        //negativeSound.start()
                }


                geciciViewListesi.clear()
                geciciViewListesi2.clear()
                geciciViewListesi3.clear()
                kaydirilanlar.clear()
                mRunnable.run()
            }
            true
        }
        val nesneKosulListener = OnTouchListener { Nesne: View, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                kop(Nesne.id.toShort())
                tumCocuklarinIsiklariniSariYap(Nesne.id.toShort())
                //Nesne.isik.setImageResource(R.drawable.sariisik)
                Nesne.bringToFront()
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                view3.visibility = INVISIBLE
                //trash.visibility = VISIBLE
                Nesne.y = motionEvent.rawY - Nesne.height
                Nesne.x = motionEvent.rawX - Nesne.width * 0.5.toFloat()

                    for (HerbirNesne: Nesne in nesneListesi) {
                        //(HerbirNesne.nesneTuru==1.toShort()||HerbirNesne.nesneTuru==3.toShort())
                        if (HerbirNesne.nesneTuru==1.toShort()||HerbirNesne.nesneTuru==3.toShort()) {
                            if (HerbirNesne.nesneResim.y - ileritek.height / 3 < Nesne.y && HerbirNesne.nesneResim.y + ileritek.height / 3 > Nesne.y)//yakınlarda başa nesne varsa
                            {
                                if (Nesne.x > HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 0.8 && Nesne.x < HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 3 / 2) {
                                    view3.x = HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width - HerbirNesne.nesneResim.width / 13
                                    view3.y = HerbirNesne.nesneResim.y + Nesne.height / 20
                                    view3.visibility = VISIBLE
                                    view3.bringToFront()
                                    }
                                }
                            }
                        }



                if (Nesne.x >= trash.x - Nesne.width && Nesne.y >= trash.y - Nesne.height) {
                    trash.setImageResource(R.drawable.blockly_trash_open)
                } else {
                    trash.setImageResource(R.drawable.blockly_trash)
                }

            }
            else {
                //trash.visibility = INVISIBLE
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {//elinikaldırdığımda
                view3.visibility = INVISIBLE
                var yapistimi=false
                if (Nesne.x >= trash.x - Nesne.width && Nesne.y >= trash.y - Nesne.height) {//sil
                    Nesne.visibility = GONE
                    frameLayout.removeView(Nesne)
                    yokol(Nesne.id.toShort())
                    //trash.visibility = INVISIBLE
                    trashSound.start()
                    trash.setImageResource(R.drawable.blockly_trash)
                }
                else {
                    Nesne.isik.setImageResource(R.drawable.kirmiziisik)
                    tumCocuklariPasifYap(Nesne.id.toShort())
                    for (HerbirNesne: Nesne in nesneListesi) {
                        if (HerbirNesne.nesneTuru==1.toShort()||HerbirNesne.nesneTuru==3.toShort()) {
                            if (HerbirNesne.nesneResim.y - ileritek.height / 3 < Nesne.y && HerbirNesne.nesneResim.y + ileritek.height / 3 > Nesne.y)//yakınlarda başa nesne varsa
                            {
                                if (Nesne.x > HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 0.8 && Nesne.x < HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 3 / 2) {
                                    Nesne.x = HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width - HerbirNesne.nesneResim.width / 13
                                    Nesne.y = HerbirNesne.nesneResim.y + Nesne.height / 20
                                    for (i in nesneListesi) {
                                        if(HerbirNesne.nesneSartId==i.nesneId){
                                            i.nesneResim.x+=sart1tek.width
                                            i.parent=null
                                        }
                                    }
                                    kosulYapis(Nesne.id.toShort(), HerbirNesne.nesneId)
                                    clickSound.start()
                                    yapistimi=true
                                    Nesne.isClickable = false
                                    object : CountDownTimer(200, 200) {
                                        override fun onFinish() {
                                            Nesne.isClickable = true
                                        }

                                        override fun onTick(millisUntilFinished: Long) {
                                        }
                                    }
                                }
                            }
                        }

                    }
                    mRunnable.run()
                    //if(!yapistimi)
                        //negativeSound.start()
                }


            }
            true
        }
        val nesneAdetListener = OnTouchListener { Nesne: View, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                kop(Nesne.id.toShort())
                tumCocuklarinIsiklariniSariYap(Nesne.id.toShort())
                //Nesne.isik.setImageResource(R.drawable.sariisik)
                Nesne.bringToFront()
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                view3.visibility = INVISIBLE
                //trash.visibility = VISIBLE
                Nesne.y = motionEvent.rawY - Nesne.height
                Nesne.x = motionEvent.rawX - Nesne.width * 0.5.toFloat()

                for (HerbirNesne: Nesne in nesneListesi) {
                    if (HerbirNesne.nesneTuru==3.toShort()) {
                        if (HerbirNesne.nesneResim.y - ileritek.height / 3 < Nesne.y && HerbirNesne.nesneResim.y + ileritek.height / 3 > Nesne.y)//yakınlarda başa nesne varsa
                        {
                            if (Nesne.x > HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 0.8 && Nesne.x < HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 3 / 2) {
                                view3.x = HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width - HerbirNesne.nesneResim.width / 13
                                view3.y = HerbirNesne.nesneResim.y + Nesne.height / 20
                                view3.visibility = VISIBLE
                                view3.bringToFront()
                            }
                        }
                    }
                }



                if (Nesne.x >= trash.x - Nesne.width && Nesne.y >= trash.y - Nesne.height) {
                    trash.setImageResource(R.drawable.blockly_trash_open)
                } else {
                    trash.setImageResource(R.drawable.blockly_trash)
                }

            }
            else {
                //trash.visibility = INVISIBLE
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {//elinikaldırdığımda
                var yapistimi=false
                view3.visibility = INVISIBLE
                if (Nesne.x >= trash.x - Nesne.width && Nesne.y >= trash.y - Nesne.height) {//sil
                    Nesne.visibility = GONE
                    frameLayout.removeView(Nesne)
                    yokol(Nesne.id.toShort())
                    //trash.visibility = INVISIBLE
                    trashSound.start()
                    trash.setImageResource(R.drawable.blockly_trash)
                }
                else {
                    Nesne.isik.setImageResource(R.drawable.kirmiziisik)
                    tumCocuklariPasifYap(Nesne.id.toShort())
                    for (HerbirNesne: Nesne in nesneListesi) {
                        if  (HerbirNesne.nesneTuru==3.toShort()){
                            if (HerbirNesne.nesneResim.y - ileritek.height / 3 < Nesne.y && HerbirNesne.nesneResim.y + ileritek.height / 3 > Nesne.y)//yakınlarda başa nesne varsa
                            {
                                if (Nesne.x > HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 0.8 && Nesne.x < HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width * 3 / 2) {
                                    Nesne.x = HerbirNesne.nesneResim.x + HerbirNesne.nesneResim.width - HerbirNesne.nesneResim.width / 13
                                    Nesne.y = HerbirNesne.nesneResim.y + Nesne.height / 20
                                    for (i in nesneListesi) {
                                        if(HerbirNesne.nesneSartId==i.nesneId){
                                            i.nesneResim.x+=sart1tek.width
                                            i.parent=null
                                        }
                                    }
                                    kosulYapis(Nesne.id.toShort(), HerbirNesne.nesneId)
                                    clickSound.start()
                                    yapistimi=true
                                    Nesne.isClickable = false
                                    object : CountDownTimer(200, 200) {
                                        override fun onFinish() {
                                            Nesne.isClickable = true
                                        }

                                        override fun onTick(millisUntilFinished: Long) {
                                        }
                                    }
                                }
                            }
                        }

                    }
                    mRunnable.run()
                    //if(!yapistimi)
                        //negativeSound.start()
                }

            }
            true
        }
        val kalemListener = OnTouchListener { Nesne: View, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                pencil.y = motionEvent.rawY - pencil.height
                pencil.x = motionEvent.rawX - pencil.width * 0.5.toFloat()
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {//elinikaldırdığımda
                for (HerbirNesne: Nesne in nesneListesi) {
                    if (HerbirNesne.yapisibil ){
                        if (HerbirNesne.kod=="karsilastirds") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    anaDeg=HerbirNesne.degisken
                                    degiskenDeger=HerbirNesne.secim
                                    karsilastirmaOperatoru=HerbirNesne.karsilastirmaOperatoru
                                    if(HerbirNesne.degisken==0.toShort())
                                        degiskenkarsilastir.setImageResource(R.drawable.elma)
                                    else if (HerbirNesne.degisken==1.toShort())
                                        degiskenkarsilastir.setImageResource(R.drawable.armut)
                                    else if (HerbirNesne.degisken==2.toShort())
                                        degiskenkarsilastir.setImageResource(R.drawable.portakal)
                                    else if (HerbirNesne.degisken==3.toShort())
                                        degiskenkarsilastir.setImageResource(R.drawable.seftali)
                                    else if (HerbirNesne.degisken==4.toShort())
                                        degiskenkarsilastir.setImageResource(R.drawable.muz)
                                    if(HerbirNesne.karsilastirmaOperatoru==1.toShort())
                                        isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esit)
                                    else if (HerbirNesne.karsilastirmaOperatoru==2.toShort())
                                        isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esitdegil)
                                    else if (HerbirNesne.karsilastirmaOperatoru==3.toShort())
                                        isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyuktur)
                                    else if (HerbirNesne.karsilastirmaOperatoru==4.toShort())
                                        isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyukesit)
                                    else if (HerbirNesne.karsilastirmaOperatoru==5.toShort())
                                        isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucuktur)
                                    else if (HerbirNesne.karsilastirmaOperatoru==6.toShort())
                                        isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucukesit)
                                    tvDegiskenSayi.text= HerbirNesne.secim.toString()
                                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                                    pencereKacDefa.visibility=View.INVISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.VISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                }
                            }
                        }
                        else if (HerbirNesne.kod=="karsilastirdd") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    anaDeg=HerbirNesne.degisken
                                    birinciDeg=HerbirNesne.birinciSayi as Short
                                    karsilastirmaOperatoru=HerbirNesne.karsilastirmaOperatoru
                                    if(anaDeg==0.toShort())
                                        degiskendegiskenkarsilastir.setImageResource(R.drawable.elma)
                                    else if (HerbirNesne.degisken==1.toShort())
                                        degiskendegiskenkarsilastir.setImageResource(R.drawable.armut)
                                    else if (HerbirNesne.degisken==2.toShort())
                                        degiskendegiskenkarsilastir.setImageResource(R.drawable.portakal)
                                    else if (HerbirNesne.degisken==3.toShort())
                                        degiskendegiskenkarsilastir.setImageResource(R.drawable.seftali)
                                    else if (HerbirNesne.degisken==4.toShort())
                                        degiskendegiskenkarsilastir.setImageResource(R.drawable.muz)

                                    if(HerbirNesne.karsilastirmaOperatoru==1.toShort())
                                        isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esit)
                                    else if (HerbirNesne.karsilastirmaOperatoru==2.toShort())
                                        isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esitdegil)
                                    else if (HerbirNesne.karsilastirmaOperatoru==3.toShort())
                                        isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyuktur)
                                    else if (HerbirNesne.karsilastirmaOperatoru==4.toShort())
                                        isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyukesit)
                                    else if (HerbirNesne.karsilastirmaOperatoru==5.toShort())
                                        isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucuktur)
                                    else if (HerbirNesne.karsilastirmaOperatoru==6.toShort())
                                        isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucukesit)
                                    if(HerbirNesne.birinciSayi==0.toShort())
                                        degiskendegiskenkarsilastir2.setImageResource(R.drawable.elma)
                                    else if (HerbirNesne.birinciSayi==1.toShort())
                                        degiskendegiskenkarsilastir2.setImageResource(R.drawable.armut)
                                    else if (HerbirNesne.birinciSayi==2.toShort())
                                        degiskendegiskenkarsilastir2.setImageResource(R.drawable.portakal)
                                    else if (HerbirNesne.birinciSayi==3.toShort())
                                        degiskendegiskenkarsilastir2.setImageResource(R.drawable.seftali)
                                    else if (HerbirNesne.birinciSayi==4.toShort())
                                        degiskendegiskenkarsilastir2.setImageResource(R.drawable.muz)
                                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                                    pencereKacDefa.visibility=View.INVISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.VISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                }
                            }
                        }
                        else if (HerbirNesne.kod=="adet") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    adet=HerbirNesne.adet
                                    tvAdet.text=adet.toString()
                                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                                    pencereKacDefa.visibility=View.VISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                }
                            }
                        }
                        else if (HerbirNesne.kod=="matematikDegSayi") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    if(HerbirNesne.degisken==0.toShort())
                                        anadegiskendegtosayi.setImageResource(R.drawable.elma)
                                    else if (HerbirNesne.degisken==1.toShort())
                                        anadegiskendegtosayi.setImageResource(R.drawable.armut)
                                    else if (HerbirNesne.degisken==2.toShort())
                                        anadegiskendegtosayi.setImageResource(R.drawable.portakal)
                                    else if (HerbirNesne.degisken==3.toShort())
                                        anadegiskendegtosayi.setImageResource(R.drawable.seftali)
                                    else if (HerbirNesne.degisken==4.toShort())
                                        anadegiskendegtosayi.setImageResource(R.drawable.muz)
                                    if(HerbirNesne.birinciSayi=="elma")
                                        anadegiskendegtosayi2.setImageResource(R.drawable.elma)
                                    else if(HerbirNesne.birinciSayi=="armut")
                                        anadegiskendegtosayi2.setImageResource(R.drawable.armut)
                                    else if(HerbirNesne.birinciSayi=="portakal")
                                        anadegiskendegtosayi2.setImageResource(R.drawable.portakal)
                                    else if(HerbirNesne.birinciSayi=="seftali")
                                        anadegiskendegtosayi2.setImageResource(R.drawable.seftali)
                                    else if(HerbirNesne.birinciSayi=="muz")
                                        anadegiskendegtosayi2.setImageResource(R.drawable.muz)
                                    if(HerbirNesne.islem == 1.toShort()){
                                        isaretmatdegtosayi.setImageResource(R.drawable.arti)
                                    }
                                    else if(HerbirNesne.islem ==2.toShort()){
                                        isaretmatdegtosayi.setImageResource(R.drawable.eksi)
                                    }
                                    else if(HerbirNesne.islem ==3.toShort()){
                                        isaretmatdegtosayi.setImageResource(R.drawable.carp)
                                    }
                                    ikincidegiskendegtosayi.text=HerbirNesne.ikinciSayi.toString()
                                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                                    pencereMatematikDegToSayi.visibility=View.VISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                                    pencereKacDefa.visibility=View.INVISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                    scrollMatematik.visibility = INVISIBLE
                                }
                            }
                        }
                        else if (HerbirNesne.kod=="matematikDegDeg") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    if(HerbirNesne.degisken==0.toShort())
                                        anadegisken.setImageResource(R.drawable.elma)
                                    else if (HerbirNesne.degisken==1.toShort())
                                        anadegisken.setImageResource(R.drawable.armut)
                                    else if (HerbirNesne.degisken==2.toShort())
                                        anadegisken.setImageResource(R.drawable.portakal)
                                    else if (HerbirNesne.degisken==3.toShort())
                                        anadegisken.setImageResource(R.drawable.seftali)
                                    else if (HerbirNesne.degisken==4.toShort())
                                        anadegisken.setImageResource(R.drawable.muz)
                                    if(HerbirNesne.birinciSayi=="elma")
                                        anadegisken2.setImageResource(R.drawable.elma)
                                    else if(HerbirNesne.birinciSayi=="armut")
                                        anadegisken2.setImageResource(R.drawable.armut)
                                    else if(HerbirNesne.birinciSayi=="portakal")
                                        anadegisken2.setImageResource(R.drawable.portakal)
                                    else if(HerbirNesne.birinciSayi=="seftali")
                                        anadegisken2.setImageResource(R.drawable.seftali)
                                    else if(HerbirNesne.birinciSayi=="muz")
                                        anadegisken2.setImageResource(R.drawable.muz)

                                    if(HerbirNesne.islem == 1.toShort()){
                                        isaretmat.setImageResource(R.drawable.arti)
                                    }
                                    else if(HerbirNesne.islem ==2.toShort()){
                                        isaretmat.setImageResource(R.drawable.eksi)
                                    }
                                    else if(HerbirNesne.islem ==3.toShort()){
                                        isaretmat.setImageResource(R.drawable.carp)
                                    }
                                    if(HerbirNesne.ikinciSayi=="elma")
                                        ikincidegisken.setImageResource(R.drawable.elma)
                                    else if(HerbirNesne.ikinciSayi=="armut")
                                        ikincidegisken.setImageResource(R.drawable.armut)
                                    else if(HerbirNesne.ikinciSayi=="portakal")
                                        ikincidegisken.setImageResource(R.drawable.portakal)
                                    else if(HerbirNesne.ikinciSayi=="seftali")
                                        ikincidegisken.setImageResource(R.drawable.seftali)
                                    else if(HerbirNesne.ikinciSayi=="muz")
                                        ikincidegisken.setImageResource(R.drawable.muz)
                                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                                    pencereMatematikDegToDeg.visibility=View.VISIBLE
                                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                                    pencereKacDefa.visibility=View.INVISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                }
                            }
                        }
                        else if (HerbirNesne.kod=="matematikSayiSayi") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    if(HerbirNesne.degisken==0.toShort())
                                        anadegiskensayitosayi.setImageResource(R.drawable.elma)
                                    else if (HerbirNesne.degisken==1.toShort())
                                        anadegiskensayitosayi.setImageResource(R.drawable.armut)
                                    else if (HerbirNesne.degisken==2.toShort())
                                        anadegiskensayitosayi.setImageResource(R.drawable.portakal)
                                    else if (HerbirNesne.degisken==3.toShort())
                                        anadegiskensayitosayi.setImageResource(R.drawable.seftali)
                                    else if (HerbirNesne.degisken==4.toShort())
                                        anadegiskensayitosayi.setImageResource(R.drawable.muz)
                                    ikincidegiskensayitosayi2.text= HerbirNesne.birinciSayi as String
                                    if(HerbirNesne.islem == 1.toShort()){
                                        isaretmatsayitosayi.setImageResource(R.drawable.arti)
                                    }
                                    else if(HerbirNesne.islem ==2.toShort()){
                                        isaretmatsayitosayi.setImageResource(R.drawable.eksi)
                                    }
                                    else if(HerbirNesne.islem ==3.toShort()){
                                        isaretmatsayitosayi.setImageResource(R.drawable.carp)
                                    }
                                    ikincidegiskensayitosayi.text= HerbirNesne.ikinciSayi as String
                                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.VISIBLE
                                    pencereKacDefa.visibility=View.INVISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                    scrollMatematik.visibility = INVISIBLE
                                }
                            }
                        }
                        else if (HerbirNesne.kod=="degisken") {
                            if (HerbirNesne.nesneResim.y < pencil.y && HerbirNesne.nesneResim.y + HerbirNesne.nesneResim.height > pencil.y) //yakınlarda başa nesne varsa
                            {
                                if (HerbirNesne.nesneResim.x < pencil.x && HerbirNesne.nesneResim.x+HerbirNesne.nesneResim.width>pencil.x) {
                                    duzenlemeModu=true
                                    geciciNesne=HerbirNesne
                                    if (geciciNesne.degisken == 0.toShort()) {
                                        degisken.setImageResource(R.drawable.elma)
                                    }
                                    else if (geciciNesne.degisken  == 1.toShort()) {
                                        degisken.setImageResource(R.drawable.armut)
                                    }
                                    else if (geciciNesne.degisken  == 2.toShort()) {
                                        degisken.setImageResource(R.drawable.portakal)
                                    }
                                    else if (geciciNesne.degisken  == 3.toShort()) {
                                        degisken.setImageResource(R.drawable.seftali)
                                    }
                                    else if (geciciNesne.degisken == 4.toShort()) {
                                        degisken.setImageResource(R.drawable.muz)
                                    }

                                    pencereDegiskenOlustur.visibility=View.VISIBLE
                                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                                    pencereKacDefa.visibility=View.INVISIBLE
                                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                                    pencereBtBaglanti.visibility=View.INVISIBLE
                                }
                            }
                        }
                    }
                }
                pencil.y = floatingActionButton.y
                pencil.x = floatingActionButton.x
            }
            true
        }
        pencil.setOnTouchListener(kalemListener)
        var degiskenTuru=0
        up.setOnClickListener {
            for (HerbirNesne: Nesne in nesneListesi) {
                if (HerbirNesne.yapisibil==true) {
                    HerbirNesne.nesneResim.y-=ileritek.height/2
                }
            }
            difYBasla-=ileritek.height/2
        }
        down.setOnClickListener {
            for (HerbirNesne: Nesne in nesneListesi) {
                if (HerbirNesne.yapisibil==true) {
                    HerbirNesne.nesneResim.y+=ileritek.height/2
                }
            }
            difYBasla+=ileritek.height/2
        }
        reset.setOnClickListener {
            for (HerbirNesne: Nesne in nesneListesi) {
                if (HerbirNesne.yapisibil==true) {
                    HerbirNesne.nesneResim.y-=difYBasla
                }
            }
            difYBasla=0
        }
        var inflater = LayoutInflater.from(this)
        var view = inflater.inflate(R.layout.basla, frameLayout, false)
        view.layoutParams.height = btnFonksiyon.height
        view.x = 350.0F
        view.y = 50.0F
        view.id = viewId
        view.setBackgroundColor(Color.parseColor("#00000000"))
        frameLayout.addView(view)
        var yeniNesne = Nesne(view, 9, "basla", view.id.toShort(), true)
        nesneListesi.add(yeniNesne)
        frameLayout.setOnClickListener {
            if (txtHareketClicked == true) {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
            }

            if (txtKararClicked == true) {
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
            }

            if (txtMatematikClicked == true) {
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
            }

            if (txtDonguClicked == true) {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
            }

            if (txtFonksiyonClicked == true) {
                scrollFonksiyon.visibility = INVISIBLE
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
            }

            if (txtDegiskenClicked == true) {
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                scrollDegisken.visibility = INVISIBLE
            }

        }
        txtHareket.setOnClickListener {
            if (txtHareketClicked == true) {
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#df0041"))
                txtHareketClicked = false
                scrollHareket.visibility = INVISIBLE
            } else {
                open.start()
                scrollHareket.visibility = VISIBLE
                scrollHareket.bringToFront()
                scrollDegisken.visibility = INVISIBLE
                scrollFonksiyon.visibility = INVISIBLE
                scrollDongu.visibility = INVISIBLE
                scrollKarar.visibility = INVISIBLE
                scrollMatematik.visibility = INVISIBLE
                txtHareket.setBackgroundColor(111)
                txtHareket.setTextColor(Color.WHITE)
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
                txtHareketClicked = true
            }
        }
            ileritek.setOnClickListener {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.ileri, frameLayout, false)
                view.layoutParams.height = ileritek.height
                view.layoutParams.width = ileritek.width
                view.x = ileritek.x + ileritek.width
                view.y = ileritek.y + ileritek.height*0.67.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 0, "F", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            durtek.setOnClickListener {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.dur, frameLayout, false)
                view.layoutParams.height = durtek.height
                view.layoutParams.width = durtek.width
                view.x = durtek.x + durtek.width
                view.y = durtek.y + durtek.height*0.67.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 0, "D", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            sagatek.setOnClickListener {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
                //txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.saga, frameLayout, false)
                view.layoutParams.height = sagatek.height
                view.layoutParams.width = sagatek.width
                view.x = sagatek.x + sagatek.width
                view.y = sagatek.y + sagatek.height*0.67.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 0, "R", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            solatek.setOnClickListener {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
                //txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.sola, frameLayout, false)
                view.layoutParams.height = solatek.height
                view.layoutParams.width = solatek.width
                view.x = solatek.x + solatek.width
                view.y = solatek.y + solatek.height*0.67.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 0, "L", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            geritek.setOnClickListener {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
                //txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.geri, frameLayout, false)
                view.layoutParams.height = geritek.height
                view.layoutParams.width = geritek.width
                view.x = geritek.x + geritek.width
                view.y = geritek.y + geritek.height*0.67.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 0, "B", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            renkDegisTek.setOnClickListener {
                scrollHareket.visibility = INVISIBLE
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#DF0041"))
                txtHareketClicked = false
                //txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.renk, frameLayout, false)
                view.layoutParams.height = renkDegisTek.height
                view.layoutParams.width = renkDegisTek.width
                view.x = renkDegisTek.x + renkDegisTek.width
                view.y = renkDegisTek.y + renkDegisTek.height*0.67.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 0, "y", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.renkDegisKare.setOnClickListener{yeniNesne.renkDegisimi()}
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
        txtKarar.setOnClickListener {
            if (txtKararClicked) {
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                scrollKarar.visibility = INVISIBLE
            } else {
                open.start()
                scrollKarar.visibility = VISIBLE
                scrollKarar.bringToFront()
                scrollDegisken.visibility = INVISIBLE
                scrollFonksiyon.visibility = INVISIBLE
                scrollDongu.visibility = INVISIBLE
                scrollHareket.visibility = INVISIBLE
                scrollMatematik.visibility = INVISIBLE

                txtKararClicked = true
                txtKarar.setBackgroundColor(111)
                txtKarar.setTextColor(Color.WHITE)
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#df0041"))
                txtHareketClicked = false
            }
        }
            egertek.setOnClickListener {
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.kosul_kontrol, frameLayout, false)
                view.layoutParams.height = egertek.height + ((egertek.height * 1.148).roundToInt())
                view.layoutParams.width = egertek.width
                view.x = egertek.x + egertek.width * 1.3.toFloat()
                view.y = egertek.y + egertek.height * 0.7.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 1, "eger", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            /*egerDegil.setOnClickListener {
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.kosul_kontrol_olumsuz, frameLayout, false)
                view.layoutParams.height = egerDegil.height + ((egerDegil.height * 1.148).roundToInt())
                view.layoutParams.width = egerDegil.width
                view.x = egerDegil.x + egerDegil.width * 1.3.toFloat()
                view.y = egerDegil.y + egerDegil.height * 0.7.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 1, "egerdegil",false, view.id.toShort())
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }*/
            sart1tek.setOnClickListener {
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.sart1, frameLayout, false)
                view.layoutParams.height = sart1tek.height
                view.layoutParams.width = sart1tek.width
                view.x = sart1tek.x + sart1tek.width*1.2.toFloat()
                view.y = sart1tek.y + sart1tek.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 2, "Y", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.sart1Boolean.setOnClickListener {yeniNesne.istekDegisimiSart1()}
                yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)
            }
            sart2tek.setOnClickListener {
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.sart2, frameLayout, false)
                view.layoutParams.height = sart2tek.height
                view.layoutParams.width = sart2tek.width
                view.x = sart2tek.x + sart2tek.width*1.2.toFloat()
                view.y = sart2tek.y + sart2tek.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 2, "M", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.sart2Boolean.setOnClickListener {yeniNesne.istekDegisimiSart2()}
                yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)
            }
            sart3tek.setOnClickListener {
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.sart3, frameLayout, false)
                view.layoutParams.height = sart3tek.height
                view.layoutParams.width = sart3tek.width
                view.x = sart3tek.x + sart3tek.width*1.2.toFloat()
                view.y = sart3tek.y + sart3tek.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 2, "K", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.sart3Boolean.setOnClickListener {yeniNesne.istekDegisimiSart3()}
                yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)
            }
            sart_engel_tek.setOnClickListener {
            scrollKarar.visibility = INVISIBLE
            txtKarar.setBackgroundColor(Color.WHITE)
            txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
            txtKararClicked = false
            var inflater = LayoutInflater.from(this)
            var view = inflater.inflate(R.layout.sart_engel, frameLayout, false)
            view.layoutParams.height = sart3tek.height
            view.layoutParams.width = sart3tek.width
            view.x = sart_engel_tek.x + sart_engel_tek.width*1.2.toFloat()
            view.y = sart_engel_tek.y + sart_engel_tek.height*0.8.toFloat()
            viewId++
            view.id = viewId
            var yeniNesne = Nesne(view, 2, "E", view.id.toShort(), false)
            nesneListesi.add(yeniNesne)
            frameLayout.addView(view)
            //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
            yeniNesne.nesneResim.sartEngelBoolean.setOnClickListener {yeniNesne.istekDegisimiSartEngel()}
            yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)
        }
            sart_cizgi_tek.setOnClickListener {
            scrollKarar.visibility = INVISIBLE
            txtKarar.setBackgroundColor(Color.WHITE)
            txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
            txtKararClicked = false
            var inflater = LayoutInflater.from(this)
            var view = inflater.inflate(R.layout.sart_cizgi, frameLayout, false)
            view.layoutParams.height = sart_cizgi_tek.height
            view.layoutParams.width = sart_cizgi_tek.width
            view.x = sart_cizgi_tek.x + sart_cizgi_tek.width*1.2.toFloat()
            view.y = sart_cizgi_tek.y + sart_cizgi_tek.height*0.8.toFloat()
            viewId++
            view.id = viewId
            var yeniNesne = Nesne(view, 2, "O", view.id.toShort(), false)
            nesneListesi.add(yeniNesne)
            frameLayout.addView(view)
            //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
            yeniNesne.nesneResim.sartCizgiBoolean.setOnClickListener {yeniNesne.istekDegisimiSartCizgi()}
            yeniNesne.nesneResim.kareCizgi.setOnClickListener{yeniNesne.cizgiDegisimi()}
            yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)
        }
            sartdegtosayi1.setOnClickListener {
                duzenlemeModu=false
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                if(anaDeg==0.toShort())
                    degiskenkarsilastir.setImageResource(R.drawable.elma)
                else if (anaDeg==1.toShort())
                    degiskenkarsilastir.setImageResource(R.drawable.armut)
                else if (anaDeg==2.toShort())
                    degiskenkarsilastir.setImageResource(R.drawable.portakal)
                else if (anaDeg==3.toShort())
                    degiskenkarsilastir.setImageResource(R.drawable.seftali)
                else if (anaDeg==4.toShort())
                    degiskenkarsilastir.setImageResource(R.drawable.muz)

                if(karsilastirmaOperatoru==1.toShort())
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esit)
                else if (karsilastirmaOperatoru==2.toShort())
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esitdegil)
                else if (karsilastirmaOperatoru==3.toShort())
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyuktur)
                else if (karsilastirmaOperatoru==4.toShort())
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyukesit)
                else if (karsilastirmaOperatoru==5.toShort())
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucuktur)
                else if (karsilastirmaOperatoru==6.toShort())
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucukesit)

                tvDegiskenSayi.text= degiskenDeger.toString()
                pencereDegiskenOlustur.visibility=View.INVISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.VISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
            }
            yukaridegiskensayi.setOnClickListener {
                numberSound.start()
                if(anaDeg==0.toShort()){
                    anaDeg=1
                    degiskenkarsilastir.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=2
                    degiskenkarsilastir.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=3
                    degiskenkarsilastir.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=4
                    degiskenkarsilastir.setImageResource(R.drawable.muz)
                }
                else if(anaDeg==4.toShort()){
                    anaDeg=0
                    degiskenkarsilastir.setImageResource(R.drawable.elma)
                }
            }
            yukaridegiskendegisken.setOnClickListener{
                numberSound.start()
                if(anaDeg==0.toShort()){
                    anaDeg=1
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=2
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=3
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=4
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.muz)
                }
                else if(anaDeg==4.toShort()){
                    anaDeg=0
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.elma)
                }
            }
            asagidegisken.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    degiskenkarsilastir.setImageResource(R.drawable.seftali)
                }
                if(anaDeg==3.toShort()){
                    anaDeg=2
                    degiskenkarsilastir.setImageResource(R.drawable.portakal)
                }
                if(anaDeg==2.toShort()){
                    anaDeg=1
                    degiskenkarsilastir.setImageResource(R.drawable.armut)
                }
                if(anaDeg==1.toShort()){
                    anaDeg=0
                    degiskenkarsilastir.setImageResource(R.drawable.elma)
                }
                if(anaDeg==0.toShort()){
                    anaDeg=4
                    degiskenkarsilastir.setImageResource(R.drawable.muz)
                }
            }
            asagidegiskendegisken.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.seftali)
                }
                if(anaDeg==3.toShort()){
                    anaDeg=2
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.portakal)
                }
                if(anaDeg==2.toShort()){
                    anaDeg=1
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.armut)
                }
                if(anaDeg==1.toShort()){
                    anaDeg=0
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.elma)
                }
                if(anaDeg==0.toShort()){
                    anaDeg=4
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.muz)
                }
            }
            yukaridegiskensayiisaret.setOnClickListener {
                numberSound.start()
                if(karsilastirmaOperatoru==1.toShort()){
                    karsilastirmaOperatoru=2
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esitdegil)
                }else if(karsilastirmaOperatoru==2.toShort()){
                    karsilastirmaOperatoru=3
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyuktur)
                }else if(karsilastirmaOperatoru==3.toShort()){
                    karsilastirmaOperatoru=4
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyukesit)
                }else if(karsilastirmaOperatoru==4.toShort()){
                    karsilastirmaOperatoru=5
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucuktur)
                }else if(karsilastirmaOperatoru==5.toShort()){
                    karsilastirmaOperatoru=6
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucukesit)
                }else if(karsilastirmaOperatoru==6.toShort()){
                    karsilastirmaOperatoru=1
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esit)
                }
            }
            yukaridegiskensdegiskenisaret.setOnClickListener {
                numberSound.start()
                if(karsilastirmaOperatoru==1.toShort()){
                    karsilastirmaOperatoru=2
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esitdegil)
                }else if(karsilastirmaOperatoru==2.toShort()){
                    karsilastirmaOperatoru=3
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyuktur)
                }else if(karsilastirmaOperatoru==3.toShort()){
                    karsilastirmaOperatoru=4
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyukesit)
                }else if(karsilastirmaOperatoru==4.toShort()){
                    karsilastirmaOperatoru=5
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucuktur)
                }else if(karsilastirmaOperatoru==5.toShort()){
                    karsilastirmaOperatoru=6
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucukesit)
                }else if(karsilastirmaOperatoru==6.toShort()){
                    karsilastirmaOperatoru=1
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esit)
                }
            }
            asagidegiskensayiisaret.setOnClickListener {
                numberSound.start()
                if(karsilastirmaOperatoru==3.toShort()){
                    karsilastirmaOperatoru=2
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esitdegil)
                }else if(karsilastirmaOperatoru==4.toShort()){
                    karsilastirmaOperatoru=3
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyuktur)
                }else if(karsilastirmaOperatoru==5.toShort()){
                    karsilastirmaOperatoru=4
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.buyukesit)
                }else if(karsilastirmaOperatoru==6.toShort()){
                    karsilastirmaOperatoru=5
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucuktur)
                }else if(karsilastirmaOperatoru==1.toShort()){
                    karsilastirmaOperatoru=6
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.kucukesit)
                }else if(karsilastirmaOperatoru==2.toShort()){
                    karsilastirmaOperatoru=1
                    isaretDegiskenSayiKarsilastir.setImageResource(R.drawable.esit)
                }
            }
            asagidegiskendegiskenisaret.setOnClickListener {
                numberSound.start()
                if(karsilastirmaOperatoru==3.toShort()){
                    karsilastirmaOperatoru=2
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esitdegil)
                }else if(karsilastirmaOperatoru==4.toShort()){
                    karsilastirmaOperatoru=3
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyuktur)
                }else if(karsilastirmaOperatoru==5.toShort()){
                    karsilastirmaOperatoru=4
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyukesit)
                }else if(karsilastirmaOperatoru==6.toShort()){
                    karsilastirmaOperatoru=5
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucuktur)
                }else if(karsilastirmaOperatoru==1.toShort()){
                    karsilastirmaOperatoru=6
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucukesit)
                }else if(karsilastirmaOperatoru==2.toShort()){
                    karsilastirmaOperatoru=1
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esit)
                }
            }
            yukaridegiskensayideger.setOnClickListener{
                if(degiskenDeger<100){
                    degiskenDeger++
                    tvDegiskenSayi.text=degiskenDeger.toString()
                }

            }
            yukaridegiskendegisken2.setOnClickListener{
                numberSound.start()
                if(birinciDeg==0.toShort()){
                    birinciDeg=1
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.armut)
                }
                else if(birinciDeg==1.toShort()){
                    birinciDeg=2
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.portakal)
                }
                else if(birinciDeg==2.toShort()){
                    birinciDeg=3
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.seftali)
                }
                else if(birinciDeg==3.toShort()){
                    birinciDeg=4
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.muz)
                }
                else if(birinciDeg==4.toShort()){
                    birinciDeg=0
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.elma)
                }
            }
            asagidegiskensayideger.setOnClickListener{
                if(degiskenDeger>1){
                    degiskenDeger--
                    tvDegiskenSayi.text=degiskenDeger.toString()
                }

            }
            asagidegiskendegisken2.setOnClickListener{
                numberSound.start()
                if(birinciDeg==2.toShort()){
                    birinciDeg=1
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.armut)
                }
                else if(birinciDeg==3.toShort()){
                    birinciDeg=2
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.portakal)
                }
                else if(birinciDeg==4.toShort()){
                    birinciDeg=3
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.seftali)
                }
                else if(birinciDeg==0.toShort()){
                    birinciDeg=4
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.muz)
                }
                else if(birinciDeg==1.toShort()){
                    birinciDeg=0
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.elma)
                }
            }
            btDegIptalDegiskenSayiKarsilastir.setOnClickListener {
                duzenlemeModu=false
                pencereDegiskenSayiKarsilastir.visibility = View.INVISIBLE
            }
            btDegTamamDegiskenSayiKarsilastir.setOnClickListener{
                pencereDegiskenSayiKarsilastir.visibility = View.INVISIBLE
                if(!duzenlemeModu) {
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.sartdegtosayi, frameLayout, false)
                    view.layoutParams.height = sartdegtosayi1.height
                    view.layoutParams.width = sartdegtosayi1.width
                    view.x = sartdegtosayi1.x + sartdegtosayi1.width * 1.2.toFloat()
                    view.y = sartdegtosayi1.y + sartdegtosayi1.height * 0.8.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 4, "karsilastirds", view.id.toShort(), anaDeg, karsilastirmaOperatoru, degiskenDeger)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.sartdegtosayiBoolean.setOnClickListener {yeniNesne.istekDegisimiSartDegToSayi()}
                    yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)


                }
                else{
                    geciciNesne.degiskenSayiKarsilastirDuzenle(anaDeg, karsilastirmaOperatoru, degiskenDeger)

                }

                duzenlemeModu=false
            }
            sartdegtodeg1.setOnClickListener {
                duzenlemeModu=false
                scrollKarar.visibility = INVISIBLE
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                if(anaDeg==0.toShort())
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.elma)
                else if (anaDeg==1.toShort())
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.armut)
                else if (anaDeg==2.toShort())
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.portakal)
                else if (anaDeg==3.toShort())
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.seftali)
                else if (anaDeg==4.toShort())
                    degiskendegiskenkarsilastir.setImageResource(R.drawable.muz)

                if(karsilastirmaOperatoru==1.toShort())
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esit)
                else if (karsilastirmaOperatoru==2.toShort())
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.esitdegil)
                else if (karsilastirmaOperatoru==3.toShort())
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyuktur)
                else if (karsilastirmaOperatoru==4.toShort())
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.buyukesit)
                else if (karsilastirmaOperatoru==5.toShort())
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucuktur)
                else if (karsilastirmaOperatoru==6.toShort())
                    isaretDegiskenDegiskenKarsilastir.setImageResource(R.drawable.kucukesit)

                if(birinciDeg==0.toShort())
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.elma)
                else if (birinciDeg==1.toShort())
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.armut)
                else if (birinciDeg==2.toShort())
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.portakal)
                else if (birinciDeg==3.toShort())
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.seftali)
                else if (birinciDeg==4.toShort())
                    degiskendegiskenkarsilastir2.setImageResource(R.drawable.muz)


                pencereDegiskenOlustur.visibility=View.INVISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.VISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
            }
            btDegIptalDegiskenDegiskenKarsilastir.setOnClickListener {
                pencereDegiskenDegiskenKarsilastir.visibility = View.INVISIBLE
                duzenlemeModu=false
            }
            btDegTamamDegiskendDegiskenKarsilastir.setOnClickListener {
                pencereDegiskenDegiskenKarsilastir.visibility = View.INVISIBLE
                if(!duzenlemeModu) {
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.sartdegtodeg, frameLayout, false)
                    view.layoutParams.height = sartdegtodeg1.height
                    view.layoutParams.width = sartdegtodeg1.width
                    view.x = sartdegtodeg1.x + sartdegtodeg1.width * 1.2.toFloat()
                    view.y = sartdegtodeg1.y + sartdegtodeg1.height * 0.8.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 4, "karsilastirdd", view.id.toShort(), anaDeg, karsilastirmaOperatoru, birinciDeg)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.sartdegtodegBoolean.setOnClickListener {yeniNesne.istekDegisimiSartDegToDeg()}
                    yeniNesne.nesneResim.setOnTouchListener(nesneKosulListener)


                }
                else{
                    geciciNesne.degiskenDegiskenKarsilastirDuzenle(anaDeg, karsilastirmaOperatoru, birinciDeg)
                }
                duzenlemeModu=false
            }
        txtMatematik.setOnClickListener {
            if (txtMatematikClicked == true) {
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
                scrollMatematik.visibility = INVISIBLE
            } else {
                open.start()
                scrollMatematik.visibility = VISIBLE
                scrollMatematik.bringToFront()
                scrollDegisken.visibility = INVISIBLE
                scrollFonksiyon.visibility = INVISIBLE
                scrollDongu.visibility = INVISIBLE
                scrollKarar.visibility = INVISIBLE
                scrollHareket.visibility= INVISIBLE
                /*
                scrollMatematik.visibility = VISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false

                */
                txtMatematik.setBackgroundColor(111)
                txtMatematik.setTextColor(Color.WHITE)
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#df0041"))
                txtHareketClicked = false
                txtMatematikClicked = true
            }
        }
            btDegTamamMatdegtosayi.setOnClickListener {
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                if(!duzenlemeModu) {
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.deg_islem_sayi, frameLayout, false)
                    //anaDegiskenIslemToSayi.setImageResource(R.drawable.elma)
                    //degTextIslemToSayi.text= ikinciDeg.toString()
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 5, "matematikDegSayi", view.id.toShort(), birinciDeg, ikinciSayi, islemMat, anaDeg)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                    //elmaText.setOnTouchListener(elmaListener)
                }
                else{
                    geciciNesne.degIslemSayiDuzenle(birinciDeg, ikinciSayi, islemMat, anaDeg)
                }
                duzenlemeModu=false

            }
            btDegIptalMatdegtosayi.setOnClickListener {
                pencereMatematikDegToSayi.visibility=View.INVISIBLE/*
                if (degiskenTuru==0)//elma ise
                {
                    elmaDeger=tvDegisken.text.toString().toInt()
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.elma, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = nesne(view, 5, "F", view.id, elmaDeger,"elma")
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                    //elmaText.setOnTouchListener(elmaListener)
                }
                if (degiskenTuru==1)//armut ise
                {
                    armutDeger=tvDegisken.text.toString().toInt()
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.armut, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = nesne(view, 5, "F", view.id, armutDeger,"armut")
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                }
                if (degiskenTuru==2)//portakal ise
                {
                    portakalDeger=tvDegisken.text.toString().toInt()
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.portakal, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = nesne(view, 5, "F", view.id, portakalDeger,"portakal")
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)

                }
                if (degiskenTuru==3)//muz ise
                {
                    muzDeger=tvDegisken.text.toString().toInt()
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.muz, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = nesne(view, 5, "F", view.id, muzDeger,"muz")
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                }
                if (degiskenTuru==4)//seftali ise
                {
                    seftaliDeger=tvDegisken.text.toString().toInt()
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.seftali, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = nesne(view, 5, "F", view.id, seftaliDeger,"seftali")
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                }
            */
                duzenlemeModu=false
            }
            btDegTamamMat.setOnClickListener {
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                if(!duzenlemeModu) {

                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.deg_islem_deg, frameLayout, false)
                    //anaDegiskenIslemToSayi.setImageResource(R.drawable.elma)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 5, "matematikDegDeg", view.id.toShort(), birinciDeg, islemMat, ikinciDeg, anaDeg, false)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                    //elmaText.setOnTouchListener(elmaListener)
                }
                else{
                    geciciNesne.degIslemDegDuzenle(birinciDeg, islemMat, ikinciDeg, anaDeg)
                }
                duzenlemeModu=false

            }
            btDegIptalMat.setOnClickListener {
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                duzenlemeModu=false
            }
            btDegTamamMatsayitosayi.setOnClickListener {
                if(!duzenlemeModu) {
                    pencereMatematikSayiToSayi.visibility = View.INVISIBLE
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.sayi_islem_sayi, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 5, "matematikSayiSayi", view.id.toShort(), false, birinciSayi, ikinciSayi, islemMat, anaDeg)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                }
                else{
                    geciciNesne.degIslemSayiToSayi(birinciSayi, ikinciSayi, islemMat, anaDeg)
                }
                duzenlemeModu=false
            }
            btDegIptalMatsayitosayi.setOnClickListener {
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                duzenlemeModu=false

            }
            yukaridegisken.setOnClickListener {
                numberSound.start()
                if(anaDeg==0.toShort()){
                    anaDeg=1
                    degisken.setImageResource(R.drawable.armut)
                }
                else if (anaDeg==1.toShort()){
                    anaDeg=2
                    degisken.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=3
                    degisken.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=4
                    degisken.setImageResource(R.drawable.muz)
                }
                else if(anaDeg==4.toShort()){
                    anaDeg=0
                    degisken.setImageResource(R.drawable.elma)
                }
            }
            asagidegisken.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    degisken.setImageResource(R.drawable.seftali)
                }
                if(anaDeg==3.toShort()){
                    anaDeg=2
                    degisken.setImageResource(R.drawable.portakal)
                }
                if(anaDeg==2.toShort()){
                    anaDeg=1
                    degisken.setImageResource(R.drawable.armut)
                }
                if(anaDeg==1.toShort()){
                    anaDeg=0
                    degisken.setImageResource(R.drawable.elma)
                }
                if(anaDeg==0.toShort()){
                    anaDeg=4
                    degisken.setImageResource(R.drawable.muz)
                }
            }
            deg.setOnClickListener {
                duzenlemeModu=false
                //anaDeg=0
                //anadegisken.setImageResource(R.drawable.elma)
                if(anaDeg==0.toShort())
                    anadegisken.setImageResource(R.drawable.elma)
                else if (anaDeg==1.toShort())
                    anadegisken.setImageResource(R.drawable.armut)
                else if (anaDeg==2.toShort())
                    anadegisken.setImageResource(R.drawable.portakal)
                else if (anaDeg==3.toShort())
                    anadegisken.setImageResource(R.drawable.seftali)
                else if (anaDeg==4.toShort())
                    anadegisken.setImageResource(R.drawable.muz)
                if(birinciDeg==0.toShort())
                    anadegisken2.setImageResource(R.drawable.elma)
                else if(birinciDeg==1.toShort())
                    anadegisken2.setImageResource(R.drawable.armut)
                else if(birinciDeg==2.toShort())
                    anadegisken2.setImageResource(R.drawable.portakal)
                else if(birinciDeg==3.toShort())
                    anadegisken2.setImageResource(R.drawable.seftali)
                else if(birinciDeg==4.toShort())
                    anadegisken2.setImageResource(R.drawable.muz)

                if(islemMat == 1.toShort()){
                    isaretmat.setImageResource(R.drawable.arti)
                }
                else if(islemMat==2.toShort()){
                    isaretmat.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==3.toShort()){
                    isaretmat.setImageResource(R.drawable.carp)
                }
                if(ikinciDeg==0.toShort())
                    ikincidegisken.setImageResource(R.drawable.elma)
                else if(ikinciDeg==1.toShort())
                    ikincidegisken.setImageResource(R.drawable.armut)
                else if(ikinciDeg==2.toShort())
                    ikincidegisken.setImageResource(R.drawable.portakal)
                else if(ikinciDeg==3.toShort())
                    ikincidegisken.setImageResource(R.drawable.seftali)
                else if(ikinciDeg==4.toShort())
                    ikincidegisken.setImageResource(R.drawable.muz)
                pencereDegiskenOlustur.visibility=View.INVISIBLE
                        pencereMatematikDegToDeg.visibility=View.VISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#78b1dc"))
                degiskenTuru=5
            }
            yukari.setOnClickListener {
                numberSound.start()
                if(anaDeg==0.toShort()){
                    anaDeg=1
                    anadegisken.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=2
                    anadegisken.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=3
                    anadegisken.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=4
                    anadegisken.setImageResource(R.drawable.muz)
                }
                else if(anaDeg==4.toShort()){
                    anaDeg=0
                    anadegisken.setImageResource(R.drawable.elma)
                }
            }
            asagi.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    anadegisken.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=2
                    anadegisken.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=1
                    anadegisken.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=0
                    anadegisken.setImageResource(R.drawable.elma)
                }
                else if(anaDeg==0.toShort()){
                    anaDeg=4
                    anadegisken.setImageResource(R.drawable.muz)
                }
            }
            yukari3.setOnClickListener {
                numberSound.start()
                if(islemMat==1.toShort()){
                    islemMat=2
                    isaretmat.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==2.toShort()){
                    islemMat=3
                    isaretmat.setImageResource(R.drawable.carp)
                }
                else if(islemMat==3.toShort()){
                    islemMat=1
                    isaretmat.setImageResource(R.drawable.arti)
                }
            }
            asagi3.setOnClickListener {
                numberSound.start()
                if(islemMat==3.toShort()){
                    islemMat=2
                    isaretmat.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==2.toShort()){
                    islemMat=1
                    isaretmat.setImageResource(R.drawable.arti)
                }
                else if(islemMat==1.toShort()){
                    islemMat=3
                    isaretmat.setImageResource(R.drawable.carp)
                }
            }
            yukari2.setOnClickListener {
                numberSound.start()
                if(birinciDeg==0.toShort()){
                    birinciDeg=1
                    anadegisken2.setImageResource(R.drawable.armut)
                }
                else if(birinciDeg==1.toShort()){
                    birinciDeg=2
                    anadegisken2.setImageResource(R.drawable.portakal)
                }
                else if(birinciDeg==2.toShort()){
                    birinciDeg=3
                    anadegisken2.setImageResource(R.drawable.seftali)
                }
                else if(birinciDeg==3.toShort()){
                    birinciDeg=4
                    anadegisken2.setImageResource(R.drawable.muz)
                }
                else if(birinciDeg==4.toShort()){
                    birinciDeg=0
                    anadegisken2.setImageResource(R.drawable.elma)
                }
            }
            asagi2.setOnClickListener {
                numberSound.start()
                if(birinciDeg==4.toShort()){
                    birinciDeg=3
                    anadegisken2.setImageResource(R.drawable.seftali)
                }
                else if(birinciDeg==3.toShort()){
                    birinciDeg=2
                    anadegisken2.setImageResource(R.drawable.portakal)
                }
                else if(birinciDeg==2.toShort()){
                    birinciDeg=1
                    anadegisken2.setImageResource(R.drawable.armut)
                }
                else if(birinciDeg==1.toShort()){
                    birinciDeg=0
                    anadegisken2.setImageResource(R.drawable.elma)
                }
                else if(anaDeg==0.toShort()){
                    anaDeg=4
                    anadegisken2.setImageResource(R.drawable.muz)
                }
            }
            yukari4.setOnClickListener {
                numberSound.start()
                if(ikinciDeg==0.toShort()){
                    ikinciDeg=1
                    ikincidegisken.setImageResource(R.drawable.armut)
                }
                else if(ikinciDeg==1.toShort()){
                    ikinciDeg=2
                    ikincidegisken.setImageResource(R.drawable.portakal)
                }
                else if(ikinciDeg==2.toShort()){
                    ikinciDeg=3
                    ikincidegisken.setImageResource(R.drawable.seftali)
                }
                else if(ikinciDeg==3.toShort()){
                    ikinciDeg=4
                    ikincidegisken.setImageResource(R.drawable.muz)
                }
                else if(ikinciDeg==4.toShort()){
                    ikinciDeg=0
                    ikincidegisken.setImageResource(R.drawable.elma)
                }
            }
            asagi4.setOnClickListener {
                numberSound.start()
                if(ikinciDeg==4.toShort()){
                    ikinciDeg=3
                    ikincidegisken.setImageResource(R.drawable.seftali)
                }
                else if(ikinciDeg==3.toShort()){
                    ikinciDeg=2
                    ikincidegisken.setImageResource(R.drawable.portakal)
                }
                else if(ikinciDeg==2.toShort()){
                    ikinciDeg=1
                    ikincidegisken.setImageResource(R.drawable.armut)
                }
                else if(ikinciDeg==1.toShort()){
                    ikinciDeg=0
                    ikincidegisken.setImageResource(R.drawable.elma)
                }
                else if(ikinciDeg==0.toShort()){
                    ikinciDeg=4
                    ikincidegisken.setImageResource(R.drawable.muz)
                }
            }
            yukaridegiskendeger.setOnClickListener {
                numberSound.start()
                if(degiskenDeger<99){
                    degiskenDeger++
                    tvDegisken.text=degiskenDeger.toString()
                }
            }
            asagidegiskendeger.setOnClickListener {
                numberSound.start()
                if(degiskenDeger>0) {
                    degiskenDeger--
                    tvDegisken.text = degiskenDeger.toString()
                }
            }
            yukarisayitosayi.setOnClickListener {
                numberSound.start()
                if(anaDeg==0.toShort()){
                    anaDeg=1
                    anadegiskensayitosayi.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=2
                    anadegiskensayitosayi.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=3
                    anadegiskensayitosayi.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=4
                    anadegiskensayitosayi.setImageResource(R.drawable.muz)
                }
                else if(anaDeg==4.toShort()){
                    anaDeg=0
                    anadegiskensayitosayi.setImageResource(R.drawable.elma)
                }
            }
            asagisayitosayi.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    anadegiskensayitosayi.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=2
                    anadegiskensayitosayi.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=1
                    anadegiskensayitosayi.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=0
                    anadegiskensayitosayi.setImageResource(R.drawable.elma)
                }
                else if(anaDeg==0.toShort()){
                    anaDeg=4
                    anadegiskensayitosayi.setImageResource(R.drawable.muz)
                }
            }
            yukarisayitosayi3.setOnClickListener {
                numberSound.start()
                if(islemMat==1.toShort()){
                    islemMat=2
                    isaretmatsayitosayi.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==2.toShort()){
                    islemMat=3
                    isaretmatsayitosayi.setImageResource(R.drawable.carp)
                }
                else if(islemMat==2.toShort()){
                    islemMat=1
                    isaretmatsayitosayi.setImageResource(R.drawable.arti)
                }
            }
            asagisayitosayi3.setOnClickListener {
                numberSound.start()
                if(islemMat==3.toShort()){
                    islemMat=2
                    isaretmatsayitosayi.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==2.toShort()){
                    islemMat=1
                    isaretmatsayitosayi.setImageResource(R.drawable.arti)
                }
                else if(islemMat==1.toShort()){
                    islemMat=3
                    isaretmatsayitosayi.setImageResource(R.drawable.carp)
                }
            }
            yukaridegtosayi.setOnClickListener {
                numberSound.start()
                if(anaDeg==0.toShort()){
                    anaDeg=1
                    anadegiskendegtosayi.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=2
                    anadegiskendegtosayi.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=3
                    anadegiskendegtosayi.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=4
                    anadegiskendegtosayi.setImageResource(R.drawable.muz)
                }
                else if(anaDeg==4.toShort()){
                    anaDeg=0
                    anadegiskendegtosayi.setImageResource(R.drawable.elma)
                }
            }
            asagidegtosayi.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    anadegiskendegtosayi.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=2
                    anadegiskendegtosayi.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=1
                    anadegiskendegtosayi.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=0
                    anadegiskendegtosayi.setImageResource(R.drawable.elma)
                }
                else if(anaDeg==0.toShort()){
                    anaDeg=4
                    anadegiskendegtosayi.setImageResource(R.drawable.muz)
                }
            }
            yukaridegtosayi2.setOnClickListener {
                numberSound.start()
                if(birinciDeg==0.toShort()){
                    birinciDeg=1
                    anadegiskendegtosayi2.setImageResource(R.drawable.armut)
                }
                else if(birinciDeg==1.toShort()){
                    birinciDeg=2
                    anadegiskendegtosayi2.setImageResource(R.drawable.portakal)
                }
                else if(birinciDeg==2.toShort()){
                    birinciDeg=3
                    anadegiskendegtosayi2.setImageResource(R.drawable.seftali)
                }
                else if(birinciDeg==3.toShort()){
                    birinciDeg=4
                    anadegiskendegtosayi2.setImageResource(R.drawable.muz)
                }
                else if(birinciDeg==4.toShort()){
                    birinciDeg=0
                    anadegiskendegtosayi2.setImageResource(R.drawable.elma)
                }
            }
            asagidegtosayi2.setOnClickListener {
                numberSound.start()
                if(anaDeg==4.toShort()){
                    anaDeg=3
                    anadegiskendegtosayi2.setImageResource(R.drawable.seftali)
                }
                else if(anaDeg==3.toShort()){
                    anaDeg=2
                    anadegiskendegtosayi2.setImageResource(R.drawable.portakal)
                }
                else if(anaDeg==2.toShort()){
                    anaDeg=1
                    anadegiskendegtosayi2.setImageResource(R.drawable.armut)
                }
                else if(anaDeg==1.toShort()){
                    anaDeg=0
                    anadegiskendegtosayi2.setImageResource(R.drawable.elma)
                }
                else if(anaDeg==0.toShort()){
                    anaDeg=4
                    anadegiskendegtosayi2.setImageResource(R.drawable.muz)
                }
            }
            yukaridegtosayi3.setOnClickListener {
                numberSound.start()
                if(islemMat==1.toShort()){
                    islemMat=2
                    isaretmatdegtosayi.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==2.toShort()){
                    islemMat=3
                    isaretmatdegtosayi.setImageResource(R.drawable.carp)
                }
                else if(islemMat==3.toShort()){
                    islemMat=1
                    isaretmatdegtosayi.setImageResource(R.drawable.arti)
                }
            }
            asagidegtosayi3.setOnClickListener {
                numberSound.start()
                if(islemMat==3.toShort()){
                    islemMat=2
                    isaretmatdegtosayi.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==2.toShort()){
                    islemMat=1
                    isaretmatdegtosayi.setImageResource(R.drawable.arti)
                }
                else if(islemMat==1.toShort()){
                    islemMat=3
                    isaretmatdegtosayi.setImageResource(R.drawable.carp)
                }
            }
            degtosayi.setOnClickListener {
                duzenlemeModu=false
                //degiskenOlustur.degisken.setImageResource(R.drawable.elma)
                //anaDeg=0
                //anadegisken.setImageResource(R.drawable.elma)
                if(anaDeg==0.toShort())
                    anadegiskendegtosayi.setImageResource(R.drawable.elma)
                else if (anaDeg==1.toShort())
                    anadegiskendegtosayi.setImageResource(R.drawable.armut)
                else if (anaDeg==2.toShort())
                    anadegiskendegtosayi.setImageResource(R.drawable.portakal)
                else if (anaDeg==3.toShort())
                    anadegiskendegtosayi.setImageResource(R.drawable.seftali)
                else if (anaDeg==4.toShort())
                    anadegiskendegtosayi.setImageResource(R.drawable.muz)
                if(birinciDeg==0.toShort())
                    anadegiskendegtosayi2.setImageResource(R.drawable.elma)
                else if(birinciDeg==1.toShort())
                    anadegiskendegtosayi2.setImageResource(R.drawable.armut)
                else if(birinciDeg==2.toShort())
                    anadegiskendegtosayi2.setImageResource(R.drawable.portakal)
                else if(birinciDeg==3.toShort())
                    anadegiskendegtosayi2.setImageResource(R.drawable.seftali)
                else if(birinciDeg==4.toShort())
                    anadegiskendegtosayi2.setImageResource(R.drawable.muz)
                if(islemMat == 1.toShort()){
                    isaretmatdegtosayi.setImageResource(R.drawable.arti)
                }
                else if(islemMat==2.toShort()){
                    isaretmatdegtosayi.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==3.toShort()){
                    isaretmatdegtosayi.setImageResource(R.drawable.carp)
                }
                ikincidegiskendegtosayi.text=ikinciSayi.toString()
                pencereDegiskenOlustur.visibility=View.INVISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.VISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#78b1dc"))
                txtMatematikClicked= false
                degiskenTuru=5
            }
            yukaridegtosayi4.setOnClickListener {
                numberSound.start()
                if(ikinciSayi<99){
                    ikinciSayi++
                    ikincidegiskendegtosayi.text=ikinciSayi.toString()
                }
            }
            asagidegtosayi4.setOnClickListener {
                numberSound.start()
                if(ikinciSayi>0) {
                    ikinciSayi--
                    ikincidegiskendegtosayi.text = ikinciSayi.toString()
                }
            }
            sayi_to_sayi.setOnClickListener {
                duzenlemeModu=false
                //degiskenOlustur.degisken.setImageResource(R.drawable.elma)
                if(anaDeg==0.toShort())
                    anadegiskensayitosayi.setImageResource(R.drawable.elma)
                else if (anaDeg==1.toShort())
                    anadegiskensayitosayi.setImageResource(R.drawable.armut)
                else if (anaDeg==2.toShort())
                    anadegiskensayitosayi.setImageResource(R.drawable.portakal)
                else if (anaDeg==3.toShort())
                    anadegiskensayitosayi.setImageResource(R.drawable.seftali)
                else if (anaDeg==4.toShort())
                    anadegiskensayitosayi.setImageResource(R.drawable.muz)
                ikincidegiskensayitosayi2.text= birinciSayi.toString()
                if(islemMat == 1.toShort()){
                    isaretmatsayitosayi.setImageResource(R.drawable.arti)
                }
                else if(islemMat==2.toShort()){
                    isaretmatsayitosayi.setImageResource(R.drawable.eksi)
                }
                else if(islemMat==3.toShort()){
                    isaretmatsayitosayi.setImageResource(R.drawable.carp)
                }
                ikincidegiskensayitosayi.text= ikinciSayi.toString()
                pencereDegiskenOlustur.visibility=View.INVISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.VISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#78b1dc"))
                txtMatematikClicked= false
                degiskenTuru=5
            }
            yukarisayitosayi4.setOnClickListener {
                numberSound.start()
                if(ikinciSayi<99){
                    ikinciSayi++
                    ikincidegiskensayitosayi.text=ikinciSayi.toString()
                }
            }
            asagisayitosayi4.setOnClickListener {
                numberSound.start()
                if(ikinciSayi>0) {
                    ikinciSayi--
                    ikincidegiskensayitosayi.text = ikinciSayi.toString()
                }
            }
            yukarisayitosayi2.setOnClickListener {
                numberSound.start()
                if(birinciSayi<99){
                    birinciSayi++
                    ikincidegiskensayitosayi2.text=birinciSayi.toString()
                }
            }
            asagisayitosayi2.setOnClickListener{
                numberSound.start()
                if(birinciSayi>0){
                    birinciSayi--
                    ikincidegiskensayitosayi2.text=birinciSayi.toString()
                }
            }
        txtDongu.setOnClickListener {
            if (txtDonguClicked == true) {
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                scrollDongu.visibility = INVISIBLE
            } else {
                open.start()
                scrollDongu.visibility = VISIBLE
                scrollDongu.bringToFront()
                scrollDegisken.visibility = INVISIBLE
                scrollFonksiyon.visibility = INVISIBLE
                scrollKarar.visibility = INVISIBLE
                scrollHareket.visibility = INVISIBLE
                txtDonguClicked = true
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
                txtDongu.setBackgroundColor(111)
                txtDongu.setTextColor(Color.WHITE)
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#df0041"))
                txtHareketClicked = false
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
            }
        }
            tekrarlatek.setOnClickListener {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.tekrarla, frameLayout, false)
                view.layoutParams.height = tekrarlatek.height + ((tekrarlatek.height * 1.148).roundToInt())
                view.layoutParams.width = tekrarlatek.width
                view.x = tekrarlatek.x + tekrarlatek.width * 1.3.toFloat()
                view.y = tekrarlatek.y + tekrarlatek.height * 0.7.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 3, "tekrarla", view.id.toShort(), false)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            }
            adet1tek.setOnClickListener {
                duzenlemeModu=false
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                adet= 1
                tvAdet.text=adet.toString()
                pencereDegiskenOlustur.visibility=View.INVISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.VISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
            }
            yukariadet.setOnClickListener{
                numberSound.start()
                if(adet<99) {
                    adet++
                    tvAdet.text=adet.toString()
                }
            }
            asagiadet.setOnClickListener{
                numberSound.start()
                if(adet>0) {
                    adet--
                    tvAdet.text=adet.toString()
                }
            }
            btAdetTamam.setOnClickListener{
                pencereKacDefa.visibility = View.INVISIBLE
                if(!duzenlemeModu) {
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.adet, frameLayout, false)
                    view.layoutParams.height = adet1tek.height
                    view.layoutParams.width = adet1tek.width
                    view.x = adet1tek.x + adet1tek.width * 1.2.toFloat()
                    view.y = adet1tek.y + adet1tek.height * 0.8.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 4, "adet", view.id.toShort(), adet)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneAdetListener)
                }
                else{
                    geciciNesne.sayiAdetDuzenle(adet)
                }
                duzenlemeModu=false
            }
            btAdetIptal.setOnClickListener {
                pencereKacDefa.visibility=View.INVISIBLE
                duzenlemeModu=false
            }
            adetElma1.setOnClickListener {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.adetdeg, frameLayout, false)
                view.layoutParams.height = adetElma1.height
                view.layoutParams.width = adetElma1.width
                view.x = adetElma1.x + adetElma1.width*1.2.toFloat()
                view.y = adetElma1.y + adetElma1.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 4, "deg", view.id.toShort(), false,0)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneAdetListener)
            }
            adetArmut1.setOnClickListener {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.adetdeg, frameLayout, false)
                view.layoutParams.height = adetArmut1.height
                view.layoutParams.width = adetArmut1.width
                view.x = adetArmut1.x + adetArmut1.width*1.2.toFloat()
                view.y = adetArmut1.y + adetArmut1.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 4, "deg", view.id.toShort(), false,1)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneAdetListener)
            }
            adetPortakal1.setOnClickListener {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.adetdeg, frameLayout, false)
                view.layoutParams.height = adetPortakal1.height
                view.layoutParams.width = adetPortakal1.width
                view.x = adetPortakal1.x + adetPortakal1.width*1.2.toFloat()
                view.y = adetPortakal1.y + adetPortakal1.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 4, "deg", view.id.toShort(), false,2)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneAdetListener)
            }
            adetSeftali1.setOnClickListener {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.adetdeg, frameLayout, false)
                view.layoutParams.height = adetSeftali1.height
                view.layoutParams.width = adetSeftali1.width
                view.x = adetSeftali1.x + adetSeftali1.width*1.2.toFloat()
                view.y = adetSeftali1.y + adetSeftali1.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 4, "deg", view.id.toShort(), false,3)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneAdetListener)
            }
            adetMuz1.setOnClickListener {
                scrollDongu.visibility = INVISIBLE
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                var inflater = LayoutInflater.from(this)
                var view = inflater.inflate(R.layout.adetdeg, frameLayout, false)
                view.layoutParams.height = adetMuz1.height
                view.layoutParams.width = adetMuz1.width
                view.x = adetMuz1.x + adetMuz1.width*1.2.toFloat()
                view.y = adetMuz1.y + adetMuz1.height*0.8.toFloat()
                viewId++
                view.id = viewId
                var yeniNesne = Nesne(view, 4, "deg", view.id.toShort(), false,4)
                nesneListesi.add(yeniNesne)
                frameLayout.addView(view)
                //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                yeniNesne.nesneResim.setOnTouchListener(nesneAdetListener)
            }
        txtFonksiyon.setOnClickListener {
            if (txtFonksiyonClicked == true) {
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
                scrollFonksiyon.visibility = INVISIBLE
            } else {
                open.start()
                scrollFonksiyon.visibility = VISIBLE
                scrollFonksiyon.bringToFront()
                scrollDegisken.visibility = INVISIBLE
                scrollDongu.visibility = INVISIBLE
                scrollKarar.visibility = INVISIBLE
                scrollHareket.visibility = INVISIBLE
                txtFonksiyonClicked = true
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
                txtFonksiyon.setBackgroundColor(111)
                txtFonksiyon.setTextColor(Color.WHITE)
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#df0041"))
                txtHareketClicked = false
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
            }
        }
        txtDegisken.setOnClickListener {
            if (txtDegiskenClicked == true) {
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#885FC9"))
                txtDegiskenClicked = false
                scrollDegisken.visibility = INVISIBLE
            } else {
                open.start()
                scrollDegisken.visibility = VISIBLE
                scrollDegisken.bringToFront()
                scrollFonksiyon.visibility = INVISIBLE
                scrollDongu.visibility = INVISIBLE
                scrollKarar.visibility = INVISIBLE
                scrollHareket.visibility = INVISIBLE
                txtDegiskenClicked = true
                scrollMatematik.visibility = INVISIBLE
                txtMatematik.setBackgroundColor(Color.WHITE)
                txtMatematik.setTextColor(Color.parseColor("#1b75ba"))
                txtMatematikClicked = false
                txtDegisken.setBackgroundColor(111)
                txtDegisken.setTextColor(Color.WHITE)
                txtHareket.setBackgroundColor(Color.WHITE)
                txtHareket.setTextColor(Color.parseColor("#df0041"))
                txtHareketClicked = false
                txtKarar.setBackgroundColor(Color.WHITE)
                txtKarar.setTextColor(Color.parseColor("#F1A2B8"))
                txtKararClicked = false
                txtDongu.setBackgroundColor(Color.WHITE)
                txtDongu.setTextColor(Color.parseColor("#4DD14A"))
                txtDonguClicked = false
                txtFonksiyon.setBackgroundColor(Color.WHITE)
                txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
                txtFonksiyonClicked = false
            }
        }
            elmaTek.setOnClickListener {
                duzenlemeModu=false
                anaDeg= 0
                degisken.setImageResource(R.drawable.elma)
                pencereDegiskenOlustur.visibility=View.VISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollDegisken.visibility = INVISIBLE
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#a27edc"))
                txtDegiskenClicked = false
                ///degiskenTuru=5

            }
            armutTek.setOnClickListener {
                duzenlemeModu=false
                anaDeg= 1
                degisken.setImageResource(R.drawable.armut)
                pencereDegiskenOlustur.visibility=View.VISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollDegisken.visibility = INVISIBLE
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#a27edc"))
                txtDegiskenClicked = false

            }
            portakalTek.setOnClickListener {
                duzenlemeModu=false
                anaDeg= 2
                degisken.setImageResource(R.drawable.portakal)
                pencereDegiskenOlustur.visibility=View.VISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollDegisken.visibility = INVISIBLE
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#a27edc"))
                txtDegiskenClicked = false
            }
            muzTek.setOnClickListener {
                duzenlemeModu=false
                anaDeg= 4
                degisken.setImageResource(R.drawable.muz)
                pencereDegiskenOlustur.visibility=View.VISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollDegisken.visibility = INVISIBLE
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#a27edc"))
                txtDegiskenClicked = false

            }
            seftaliTek.setOnClickListener {
                duzenlemeModu=false
                anaDeg= 3
                degisken.setImageResource(R.drawable.seftali)
                pencereDegiskenOlustur.visibility=View.VISIBLE
                pencereMatematikDegToDeg.visibility=View.INVISIBLE
                pencereMatematikDegToSayi.visibility=View.INVISIBLE
                pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                pencereKacDefa.visibility=View.INVISIBLE
                pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                pencereFonksiyonOlustur.visibility=View.INVISIBLE
                pencereBtBaglanti.visibility=View.INVISIBLE
                scrollDegisken.visibility = INVISIBLE
                txtDegisken.setBackgroundColor(Color.WHITE)
                txtDegisken.setTextColor(Color.parseColor("#a27edc"))
                txtDegiskenClicked = false

            }
            btDegIptal.setOnClickListener {
                pencereDegiskenOlustur.visibility = View.INVISIBLE
                duzenlemeModu=false
            }
            btDegTamam.setOnClickListener {
                pencereDegiskenOlustur.visibility=View.INVISIBLE
                if(!duzenlemeModu) {
                    var inflater = LayoutInflater.from(this)
                    var view = inflater.inflate(R.layout.elma, frameLayout, false)
                    view.layoutParams.height = ileritek.height
                    view.layoutParams.width = ileritek.width
                    view.x = ileritek.x + ileritek.width
                    view.y = ileritek.y + ileritek.height * 0.6.toFloat()
                    viewId++
                    view.id = viewId
                    var yeniNesne = Nesne(view, 5, "degisken", view.id.toShort(), tvDegisken.text.toString().toShort(), anaDeg)
                    nesneListesi.add(yeniNesne)
                    frameLayout.addView(view)
                    //Toast.makeText(this,viewId.toString()+". nesne eklendi",Toast.LENGTH_SHORT)
                    yeniNesne.nesneResim.setOnTouchListener(nesneListener)
                    //elmaText.setOnTouchListener(elmaListener)
                }
                else{
                    geciciNesne.degiskenDuzenle(tvDegisken.text.toString().toShort(), anaDeg)

                }
                duzenlemeModu=false
            }
        audio.setOnClickListener {
            if (sound == true) {
                audio.setBackgroundResource(R.drawable.nosound)
                sound = false
                clickSound.setVolume(0F, 0F)
                trashSound.setVolume(0F, 0F)
                open.setVolume(0F, 0F)
                factorySound.setVolume(0F, 0F)
                numberSound.setVolume(0F, 0F)
            } else {
                audio.setBackgroundResource(R.drawable.sound)
                sound = true
                clickSound.setVolume(1F, 1F)
                trashSound.setVolume(1F, 1F)
                open.setVolume(1F, 1F)
                factorySound.setVolume(1F, 1F)
                numberSound.setVolume(1F, 1F)
            }
        }
        connectionImage.setOnClickListener {
            pairedDevicesArrayAdapter.clear()
            if(!mIsConnected){
                if(pencereBtBaglanti.visibility == INVISIBLE)
                {
                    var pairedDevices = mBluetoothAdapter!!.bondedDevices
                    if (pairedDevices.size > 0) {
                        for (device: BluetoothDevice in pairedDevices) {
                            if(device.name.substring(0..4) == robotName||device.name.substring(0..4) == robotName2) {
                                pairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
                            }
                        }
                    }
                    pencereDegiskenOlustur.visibility=View.INVISIBLE
                    pencereMatematikDegToDeg.visibility=View.INVISIBLE
                    pencereMatematikDegToSayi.visibility=View.INVISIBLE
                    pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                    pencereKacDefa.visibility=View.INVISIBLE
                    pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                    pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                    pencereFonksiyonOlustur.visibility=View.INVISIBLE
                    pencereBtBaglanti.visibility=View.VISIBLE
                    pencereBtBaglanti.bringToFront()
                }
                else{
                    //progressBarBt.visibility= INVISIBLE
                    pencereBtBaglanti.visibility = INVISIBLE
                    mBluetoothAdapter.cancelDiscovery()
                }
            }
            else
            {
                disconnect()
            }
        }
        play.setOnClickListener {
            mRunnable.run()
            string=""
            if(!mIsConnected) {
                Toast.makeText(this, R.string.robota_baglanmalisin, Toast.LENGTH_SHORT).show()
                if (mBluetoothAdapter == null) {
                    Toast.makeText(this, R.string.bluetooth_desteklenmiyor, Toast.LENGTH_LONG).show()
                }
                else {
                    pairedDevicesArrayAdapter.clear()
                    mBluetoothAdapter.enable()
                    mBluetoothAdapter.startDiscovery()
                    var pairedDevices = mBluetoothAdapter!!.bondedDevices
                    if (pairedDevices.size > 0) {
                        for (device: BluetoothDevice in pairedDevices) {
                            if(device.name.substring(0..4) == robotName||device.name.substring(0..4) == robotName2) {
                                pairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
                            }
                        }
                        pencereDegiskenOlustur.visibility=View.INVISIBLE
                        pencereMatematikDegToDeg.visibility=View.INVISIBLE
                        pencereMatematikDegToSayi.visibility=View.INVISIBLE
                        pencereMatematikSayiToSayi.visibility=View.INVISIBLE
                        pencereKacDefa.visibility=View.INVISIBLE
                        pencereDegiskenSayiKarsilastir.visibility=View.INVISIBLE
                        pencereDegiskenDegiskenKarsilastir.visibility=View.INVISIBLE
                        pencereFonksiyonOlustur.visibility=View.INVISIBLE
                        pencereBtBaglanti.visibility=View.VISIBLE
                        pencereBtBaglanti.bringToFront()
                    }
                    else{
                        Toast.makeText(this, R.string.eslanmiş_robot_bulunamadi, Toast.LENGTH_LONG).show()
                    }

                }
            }
            else{
                if(!playing){
                    tumCocuklariAktifYap(0)
                    elmaDeger=0
                    armutDeger=0
                    portakalDeger=0
                    seftaliDeger=0
                    muzDeger=0
                    play.setImageResource(R.drawable.bluestopbutton)
                    playing=true
                    run(0,true)
                }
                else if(playing){
                    play.setImageResource(R.drawable.blueplaybutton)
                    playing=false
                    //tumCocuklariAktifYap(0)
                    siradakiNesneListesi.clear()
                }

            }
        }
    }
    override fun onBackPressed() {
        this.finish()
        var mesaj:String = getString(R.string.see_you)
        val intent = Intent(this@FullscreenActivity,SplashScreenActivity::class.java)
        intent.putExtra("Mesaj", mesaj)
        startActivity(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (this.mBluetoothAdapter!= null) {
            if (mBluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth Kapatılıyor...", Toast.LENGTH_LONG).show()
                mBluetoothAdapter.cancelDiscovery()
                mBluetoothAdapter.disable()
            }
        }
        unregisterReceiver(mReceiver)
    }
    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Toast.makeText(this@FullscreenActivity,  R.string.baglantikesildi, Toast.LENGTH_LONG).show()
                    connectionImage.setImageResource(R.drawable.disconnected)
                    mIsConnected=false
                }

            }
        }
    }
    private fun kop(myId: Short) {
        for (i in nesneListesi) {
            if (i!!.nesneId == myId) {
                i.active = false
                i.parent = null
            } else if (i.nesneAltId == myId)
                i.nesneAltId = null
            else if (i.nesneSartId == myId)
                i.nesneSartId = null
            else if (i.nesneIcId == myId)
                i.nesneIcId = null
        }
    }
    private fun fonksiyonOlustur( x:Float,y:Float,id:Short,ad:String){
        var inflater = LayoutInflater.from(this)
        var view = inflater.inflate(R.layout.fonksiyon, frameLayout, false)
        view.layoutParams.height = ileritek.height
        view.layoutParams.width = ileritek.width
        view.x = factory.x-ileritek.width
        view.y = y
        viewId++
        view.id = viewId
        var yeniNesne = Nesne(view, 6, "F", view.id.toShort(), false,ad)
        yeniNesne.nesneFonkId=id
        nesneListesi.add(yeniNesne)
        frameLayout.addView(view)
        yeniNesne.nesneResim.setOnTouchListener(nesneListener)
        fonkListesi.add(ad)
        var inflater2 = LayoutInflater.from(this)
        var viewCopy = inflater2.inflate(R.layout.fonksiyon, fonksiyonLayout, false)
        viewCopy.layoutParams.height = ileritek.height
        viewCopy.layoutParams.width = ileritek.width
        viewCopy.x = ileritek.x+(ileritek.width*0.2143).toFloat()
        viewCopy.y = fonkListesi.size*(ileritek.y+(ileritek.height*0.667).toFloat())
        var copyNesne = Nesne(viewCopy, 6, "F", view.id.toShort(), false,ad)
        copyNesne.nesneFonkId=id
        fonksiyonLayout.addView(viewCopy)
        viewCopy.setOnClickListener {
            txtFonksiyon.setBackgroundColor(Color.WHITE)
            txtFonksiyon.setTextColor(Color.parseColor("#FFAB1A"))
            txtFonksiyonClicked = false
            scrollFonksiyon.visibility = INVISIBLE
            var inflater = LayoutInflater.from(this)
            var view = inflater.inflate(R.layout.fonksiyon, frameLayout, false)
            view.layoutParams.height = ileritek.height
            view.layoutParams.width = ileritek.width
            view.x = viewCopy.x + ileritek.width-(ileritek.width*0.23).toInt()
            view.y = viewCopy.y //+ ileritek.height*0.6.toFloat()
            viewId++
            view.id = viewId
            var yeniNesne = Nesne(view, 6, "F", view.id.toShort(), false,ad)
            yeniNesne.nesneFonkId=id
            nesneListesi.add(yeniNesne)
            frameLayout.addView(view)
            yeniNesne.nesneResim.setOnTouchListener(nesneListener)
            //fonkListesi.add(ad)
        }
    }
    private fun donus(myId: Short?) {
        if (myId == null) {
            return}
        else{
            var tempId:Short?=0
            /*
            for (i in nesneListesi) {
                if (i.nesneAltId == myId) {
                    i.nesneAltId = null

                }
                if (i.nesneSartId == myId) {
                    i.nesneSartId = null
                }
                if (i.nesneIcId == myId) {
                    i.nesneIcId = null
                }
            }*/
            for (i in nesneListesi) {
                if (i.nesneId == myId) {
                    if (i.nesneTuru==1.toShort()||i.nesneTuru==3.toShort()) {
                        for (j in nesneListesi) {
                            if (i.nesneSartId == j.nesneId) {
                                frameLayout.removeView(j.nesneResim)
                            }
                        }
                        for (j in nesneListesi) {
                            if (i.nesneIcId == j.nesneId) {
                                j.yapisibil=false
                                frameLayout.removeView(j.nesneResim)
                                donus(j.nesneId)
                            }
                        }
                    }
                    frameLayout.removeView(i.nesneResim)
                    i.yapisibil=false
                    donus(i.nesneAltId)
                }
            }

        }
    }
    private fun yokol(myId: Short?) {
        if (myId == null) {
            return}
        else{
            var tempId:Int?=0
            for (i in nesneListesi) {
                if (i.nesneAltId == myId) {
                    i.nesneAltId = null

                }
                if (i.nesneSartId == myId) {
                    i.nesneSartId = null
                }
                if (i.nesneIcId == myId) {
                    i.nesneIcId = null
                }
            }
            for (i in nesneListesi) {
                if (i.nesneId == myId) {
                    if (i.nesneTuru==1.toShort()||i.nesneTuru==3.toShort()) {
                        for (j in nesneListesi) {
                            if (i.nesneSartId == j.nesneId) {
                                frameLayout.removeView(j.nesneResim)
                            }
                        }
                        for (j in nesneListesi) {
                            if (i.nesneIcId == j.nesneId) {
                                j.yapisibil=false
                                frameLayout.removeView(j.nesneResim)
                                yokol(j.nesneId)
                            }
                        }
                    }
                    frameLayout.removeView(i.nesneResim)
                    i.yapisibil=false
                    yokol(i.nesneAltId)
                }
            }

        }
   }
    private fun yapis(myId: Short, parentId: Short) {
        for (i in nesneListesi) {
            if (i.nesneId == myId) {
                i.parent = parentId
                for (j in nesneListesi) {
                    if (j.nesneId == parentId) {
                        j.nesneAltId = myId
                        if (j.active) {
                            tumCocuklariAktifYap(i.nesneId)
                        } else {
                            tumCocuklariPasifYap(i.nesneId)
                        }
                    }
                }
            }
        }
    }
    private fun icYapis(myId: Short, parentId: Short) {
        for (i in nesneListesi) {
            if (i.nesneId == myId) {
                i.parent = parentId
                for (j in nesneListesi) {
                    if (j.nesneId == parentId) {
                        j.nesneIcId = myId

                        if (j.active == true) {
                            tumCocuklariAktifYap(i.nesneId)
                        } else {
                            tumCocuklariPasifYap(i.nesneId)
                        }
                    }
                }
            }
        }
    }
    private fun kosulYapis(myId: Short, parentId: Short) {
        for (i in nesneListesi) {
            if (i.nesneId == myId) {
                i.parent = parentId
                for (j in nesneListesi) {
                    if (j.nesneId == parentId) {
                        j.nesneSartId = myId
                        if (j.active == true) {
                            i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                            i.active = true
                        } else {
                            tumCocuklariPasifYap(i.nesneId)
                            i.active = false
                        }
                    }
                }

            }
        }

    }
    private fun tumCocuklariAktifYap(myId: Short?) {
        if (myId == null) {
            return
        } else {
            for (i in nesneListesi) {
                if (i.nesneId == myId) {
                    if ((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort()) ) {
                        for (j in nesneListesi) {
                            if (i.nesneSartId == j.nesneId) {
                                j.active = true
                                j.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                j.yapisibil=true

                            }
                        }
                        for (j in nesneListesi) {
                            if (i.nesneIcId == j.nesneId) {
                                j.active = true
                                j.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                tumCocuklariAktifYap(j.nesneId)
                                j.yapisibil=true

                            }
                        }

                    }
                    i.active = true
                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                    i.yapisibil=true
                    tumCocuklariAktifYap(i.nesneAltId)
                }
            }
        }
    }
    private fun tumCocuklariPasifYap(MyId: Short?) {
        if (MyId == null) {
            return
        } else {
            for (i in nesneListesi) {
                if (i.nesneId == MyId) {
                    if ((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort())) {
                        for (j in nesneListesi) {
                            if (i.nesneSartId == j.nesneId) {
                                j.active = false
                                j.yapisibil=true
                                j.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                            }
                        }
                        for (j in nesneListesi) {
                            if (i.nesneIcId == j.nesneId) {
                                j.active = false
                                j.yapisibil=true
                                j.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                                tumCocuklariPasifYap(j.nesneId)
                            }
                        }
                    }
                    i.active = false
                    i.yapisibil=true
                    i.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                    tumCocuklariPasifYap(i.nesneAltId)
                }
            }
        }


    }
    private fun tumCocuklarinIsiklariniSariYap(myId: Short?) {
        if (myId == null) {
            return
        } else {
            for (i in nesneListesi) {
                if (i.nesneId == myId) {
                    i.nesneResim.isik.setImageResource(R.drawable.sariisik)
                    i.nesneResim.bringToFront()
                    if ((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort()))  {
                        for (j in nesneListesi) {
                            if (i.nesneSartId == j.nesneId) {
                                j.nesneResim.isik.setImageResource(R.drawable.sariisik)
                                j.nesneResim.bringToFront()
                                j.yapisibil=false
                            }
                        }
                        for (j in nesneListesi) {
                            if (i.nesneIcId == j.nesneId) {
                                j.nesneResim.isik.setImageResource(R.drawable.sariisik)
                                j.nesneResim.bringToFront()
                                j.yapisibil=false
                                tumCocuklarinIsiklariniSariYap(j.nesneId)
                            }
                        }
                    }
                    tumCocuklarinIsiklariniSariYap(i.nesneAltId)
                }

            }
        }


    }
    private fun tumCocuklarinIsiklariniKirmiziYap(myId: Short?) {
        if (myId == null) {
            return
        } else {
            for (i in nesneListesi) {
                if (i.nesneId == myId) {

                    i.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                    i.nesneResim.bringToFront()
                    i.yapisibil=true

                    if ((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort())) {
                        for (j in nesneListesi) {
                            if (i.nesneSartId == j.nesneId) {
                                j.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                                j.nesneResim.bringToFront()
                                j.yapisibil=true

                            }
                        }
                        for (j in nesneListesi) {
                            if (i.nesneIcId == j.nesneId) {
                                j.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                                j.nesneResim.bringToFront()
                                j.yapisibil=true
                                tumCocuklarinIsiklariniKirmiziYap(j.nesneId)
                            }
                        }
                    }
                    tumCocuklarinIsiklariniKirmiziYap(i.nesneAltId)
                }
            }
        }


    }
    private fun tumCocuklariTasi(myX: Float, myY: Float): Int {
        var enAlttakiElemanY=0
        if(!geciciViewListesi.isEmpty()) {
            for (i in geciciViewListesi) {
                    val j = nesneListesi.find { x -> x.nesneId == i }
                    if (j!!.nesneResim.y > enAlttakiElemanY)
                        enAlttakiElemanY = j.nesneResim.y.toInt()
                            j.nesneResim.x += myX
                            j.nesneResim.y += myY
            }
        }
        return enAlttakiElemanY
    }
    private fun eklendigimYerdekileriBenimAltimaEkle(ekleDifY: Float) {
         if(!geciciViewListesi2.isEmpty()) {
            for (i in geciciViewListesi2) {
                for(j in nesneListesi){
                    if(j.nesneId==i)
                    {
                        j.nesneResim.y+=ekleDifY
                    }
                }

            }
        }
        else return
    }
    private fun geciciViewListesiYap(myId: Short?){
        if (myId == null) {
            return
        }
        else {
            for (i in nesneListesi) {
                if(i.parent==myId)
                {
                    geciciViewListesi.add(i.nesneId)
                    //sabitViewListesi.remove(i.nesneId)
                }
                if(i.nesneId==myId)
                {
                    geciciViewListesiYap(i.nesneAltId)
                    if((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort()))
                    {
                        for(j in nesneListesi)
                        {
                            if(i.nesneSartId==j.nesneId) {
                                geciciViewListesi.add(j.nesneId!!)
                                //sabitViewListesi.remove(j.nesneId!!)
                            }
                        }
                        for(j in nesneListesi)
                        {
                            if(i.nesneIcId==j.nesneId) {
                                geciciViewListesi.add(j.nesneId!!)
                                //sabitViewListesi.remove(j.nesneId!!)
                            }
                            geciciViewListesiYap(i.nesneIcId)
                        }
                    }
                }
            }
        }
    }
    private fun geciciViewListesiYap3(myId: Short?){
        if (myId == null) {
            return
        }
        else {
            geciciViewListesi3.add(myId)
            for (i in nesneListesi) {
                if(i.parent==myId)
                {
                    geciciViewListesi3.add(i.nesneId)
                }
                if(i.nesneId==myId)
                {
                    geciciViewListesiYap3(i.nesneAltId)
                    if((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort()))
                    {
                        for(j in nesneListesi)
                        {
                            if(i.nesneSartId==j.nesneId)
                                geciciViewListesi3.add(j.nesneId!!)
                        }
                        for(j in nesneListesi)
                        {
                            if(i.nesneIcId==j.nesneId)
                                geciciViewListesi3.add(j.nesneId!!)
                            geciciViewListesiYap3(i.nesneIcId)
                        }

                    }
                }

            }

        }
    }
    private fun asagiKaydirilacaklariSec(id: Short?){
        if (id == null) {
            return
        }
        else {
            geciciViewListesi2.add(id)
            for (i in nesneListesi) {
                if(i.parent==id)
                {
                    geciciViewListesi2.add(i.nesneId)
                    if((i.nesneTuru==1.toShort())||(i.nesneTuru==3.toShort()))
                    {
                        for(j in nesneListesi)
                        {
                            if(i.nesneSartId==j.nesneId)
                                geciciViewListesi2.add(j.nesneId!!)
                        }
                        for(j in nesneListesi)
                        {
                            if(i.nesneIcId==j.nesneId)
                                geciciViewListesi2.add(j.nesneId!!)
                            asagiKaydirilacaklariSec(i.nesneIcId)
                        }

                    }
                    asagiKaydirilacaklariSec(i.nesneAltId)
                }

            }

        }
    }
    private fun run(nesneId:Short?,istek:Boolean):Boolean{
        if(nesneId==null&&siradakiNesneListesi.isEmpty())
        {
            play.setImageResource(R.drawable.blueplaybutton)
            playing=false
            return true
        }
        else {
            val i = nesneListesi.find { x -> x.nesneId == nesneId }
            i!!.nesneResim.isik.setImageResource(R.drawable.sariisik)
                if (i.nesneTuru==9.toShort()){//başladaysa
                    tumCocuklarinIsiklariniKirmiziYap(i.nesneId)
                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                    run(i.nesneAltId,true)
                }
                else if (i.nesneTuru==5.toShort()){//matematik koduysa
                    matKodCalistir(i)
                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                    if (i.nesneAltId!=null){
                        run(i.nesneAltId,true)
                    }
                    else if(!siradakiNesneListesi.isEmpty()){
                        var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                        siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                        run(temp,true)
                    }
                    else{
                        mRunnable.run()
                        play.setImageResource(R.drawable.blueplaybutton)
                        playing=false
                        siradakiNesneListesi.clear()
                        return true
                    }

                }
                else if (i.nesneTuru==6.toShort()){//fonksiyon bloğuysa
                    if(i.nesneAltId!=null){
                        siradakiNesneListesi.add(i.nesneAltId)
                    }
                    run(i.nesneFonkId,true)
                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                }
                else if (i.nesneTuru==3.toShort()) {//while bloğuysa

                    if(i.nesneAltId!=null){
                        siradakiNesneListesi.add(i.nesneAltId)
                    }
                    if (sartKontrol(i.nesneSartId)) {
                        if(i.nesneIcId!=null){
                            siradakiNesneListesi.add(i.nesneId)
                            i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                            run(i.nesneIcId,true)
                        }
                        else {
                            siradakiNesneListesi.remove(i.nesneAltId)
                            if(i.nesneAltId!=null){
                                run(i.nesneAltId,true)
                            }
                            else if(!siradakiNesneListesi.isEmpty()) {
                                var temp:Short?=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                                siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                run(temp,true)
                            }
                            else{
                                play.setImageResource(R.drawable.blueplaybutton)
                                playing=false
                                siradakiNesneListesi.clear()
                                return true
                            }
                        }
                    }else {
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        siradakiNesneListesi.remove(i.nesneId)
                        siradakiNesneListesi.remove(i.nesneAltId)
                        if(i.nesneAltId!=null){
                            run(i.nesneAltId,true)
                        }
                        else if(!siradakiNesneListesi.isEmpty()) {
                            var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                            siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                            run(temp,true)
                        }
                        else{
                            play.setImageResource(R.drawable.blueplaybutton)
                            playing=false
                            siradakiNesneListesi.clear()
                            return true
                        }
                    }
                }
                else if (i.nesneTuru==1.toShort()) {//eğer bloğuysa
                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                    if(i.nesneAltId!=null){
                        siradakiNesneListesi.add(i.nesneAltId)
                    }
                    if(i.nesneSartId!=null) {
                        val j = nesneListesi.find { x -> x.nesneId == i.nesneSartId }
                        if (j!!.kod == "K" || j.kod == "Y" || j.kod == "M"|| j.kod == "O"|| j.kod == "S"|| j.kod == "P"|| j.kod == "E") {
                            if (i.nesneIcId != null) {
                                siradakiNesneListesi.add(i.nesneIcId)
                                run(j.nesneId,true)
                            } else {
                                siradakiNesneListesi.remove(i.nesneAltId)
                                if (i.nesneAltId != null) {
                                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                    run(i.nesneAltId,true)
                                } else if (!siradakiNesneListesi.isEmpty()) {
                                    var temp = siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1)
                                    siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1))
                                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)

                                    run(temp,true)
                                } else
                                    return true
                            }
                        }
                        else if (sartKontrol(i.nesneSartId)) {
                            if (i.nesneIcId != null) {
                                run(i.nesneIcId,true)
                            } else {
                                siradakiNesneListesi.remove(i.nesneAltId)
                                if (i.nesneAltId != null) {
                                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                    run(i.nesneAltId,true)
                                } else if (!siradakiNesneListesi.isEmpty()) {
                                    var temp = siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1)
                                    siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1))
                                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                    run(temp,true)
                                } else{
                                    play.setImageResource(R.drawable.blueplaybutton)
                                    playing=false
                                    siradakiNesneListesi.clear()
                                    return true
                                }
                            }
                        }
                        else {
                            i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                            siradakiNesneListesi.remove(i.nesneAltId)
                            if (i.nesneAltId != null) {
                                run(i.nesneAltId,true)
                            } else if (!siradakiNesneListesi.isEmpty()) {
                                var temp = siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1)
                                siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1))
                                run(temp,true)
                            } else {
                                play.setImageResource(R.drawable.blueplaybutton)
                                playing=false
                                siradakiNesneListesi.clear()
                                return true
                            }
                        }
                    }
                    else {
                            i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                            siradakiNesneListesi.remove(i.nesneAltId)
                            if (i.nesneAltId != null) {
                                run(i.nesneAltId,true)
                            } else if (!siradakiNesneListesi.isEmpty()) {
                                var temp = siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1)
                                siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size - 1))
                                run(temp,true)
                            } else{
                                play.setImageResource(R.drawable.blueplaybutton)
                                playing=false
                                siradakiNesneListesi.clear()
                                return true
                            }


                    }
                }
                else if (i.nesneTuru==0.toShort()) {//hareket komutuysa
                    if (komutGonder(i.kod)) {
                        object: CountDownTimer(3000,100) {
                                override fun onFinish() {
                                    Toast.makeText(this@FullscreenActivity, R.string.baglanti_hatasi, Toast.LENGTH_SHORT).show()
                                    play.setImageResource(R.drawable.blueplaybutton)
                                    playing=false
                                    siradakiNesneListesi.clear()
                                    return
                                }
                                override fun onTick(millisUntilFinished: Long) {
                                    if(!playing) {
                                        cancel()
                                        play.setImageResource(R.drawable.blueplaybutton)
                                        siradakiNesneListesi.clear()
                                        return
                                    }
                                    mRunnable.run()
                                    if (string.contains("f")||string.contains("b")||string.contains("l")||string.contains("r")||string.contains("t")) {
                                        cancel()
                                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                        if(i.nesneAltId!=null){
                                            run(i.nesneAltId,true)
                                        }else if(!siradakiNesneListesi.isEmpty()){
                                            var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                                            siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                            run(temp,true)
                                        }
                                        else {
                                            play.setImageResource(R.drawable.blueplaybutton)
                                            playing=false
                                            siradakiNesneListesi.clear()
                                            return
                                        }
                                    }

                                }
                            }.start()
                    }
                    else{
                        Toast.makeText(this@FullscreenActivity, R.string.baglantihatasi, Toast.LENGTH_SHORT).show()
                        return false
                    }
                }
                else if (i.nesneTuru==2.toShort()) {//şart koduysa
                    if (komutGonder(i.kod)) {
                        object: CountDownTimer(1500,100) {
                            override fun onFinish() {
                                Toast.makeText(this@FullscreenActivity, R.string.baglanti_hatasi, Toast.LENGTH_SHORT).show()
                                play.setImageResource(R.drawable.blueplaybutton)
                                playing=false
                                siradakiNesneListesi.clear()
                                return
                            }
                            override fun onTick(millisUntilFinished: Long) {
                                if(!playing) {
                                    cancel()
                                    play.setImageResource(R.drawable.blueplaybutton)
                                    siradakiNesneListesi.clear()
                                    return
                                }
                                if(i.istek){
                                    mRunnable.run()
                                    if (string.contains("1")) {
                                        cancel()
                                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                        if(!siradakiNesneListesi.isEmpty()){

                                            var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                                            siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                            run(temp,true)
                                        }
                                        else {
                                            play.setImageResource(R.drawable.blueplaybutton)
                                            playing=false
                                            siradakiNesneListesi.clear()
                                            return
                                        }
                                    }
                                    else if (string.contains("0")) {
                                        cancel()
                                        i.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                                        if(!siradakiNesneListesi.isEmpty()){
                                        siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                            if(!siradakiNesneListesi.isEmpty()){
                                                var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                                                siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                                run(temp,true)
                                            }

                                            else {
                                                play.setImageResource(R.drawable.blueplaybutton)
                                                playing=false
                                                siradakiNesneListesi.clear()
                                                return
                                                }
                                            }

                                        else {
                                            play.setImageResource(R.drawable.blueplaybutton)
                                            playing=false
                                            siradakiNesneListesi.clear()
                                            return
                                        }
                                    }
                                }
                                else if(!(i.istek)){
                                    mRunnable.run()
                                    if (string.contains("0")) {
                                        cancel()
                                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                                        if(!siradakiNesneListesi.isEmpty()){
                                            var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                                            siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                            run(temp,true)
                                        }
                                        else {
                                            play.setImageResource(R.drawable.blueplaybutton)
                                            playing=false
                                            siradakiNesneListesi.clear()
                                            return
                                        }
                                    }
                                    else if (string.contains("1")) {
                                        cancel()
                                        i.nesneResim.isik.setImageResource(R.drawable.kirmiziisik)
                                        if(!siradakiNesneListesi.isEmpty())
                                        {
                                            siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                            if(!siradakiNesneListesi.isEmpty())
                                            {
                                            var temp=siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1)
                                            siradakiNesneListesi.remove(siradakiNesneListesi.elementAt(siradakiNesneListesi.size-1))
                                            run(temp,true)
                                        }

                                        else {
                                            play.setImageResource(R.drawable.blueplaybutton)
                                            playing=false
                                            siradakiNesneListesi.clear()
                                            return
                                        }
                                    }

                                        else {
                                            play.setImageResource(R.drawable.blueplaybutton)
                                            playing=false
                                            siradakiNesneListesi.clear()
                                            return
                                        }
                                    }
                                }

                            }
                        }.start()
                    }
                    else{
                        Toast.makeText(this@FullscreenActivity, R.string.baglantihatasi, Toast.LENGTH_SHORT).show()
                        play.setImageResource(R.drawable.blueplaybutton)
                        siradakiNesneListesi.clear()
                        return false
                    }
                }
            return true
        }
    }
    private fun matKodCalistir(nesne: Nesne) {
        if(nesne.degisken==0.toShort()){
            if(nesne.islem==1.toShort()){
                if(nesne.birinciSayi=="elma"){
                if(nesne.ikinciSayi=="elma")
                        elmaDeger = (elmaDeger+elmaDeger).toShort()
                else if(nesne.ikinciSayi=="armut")
                        elmaDeger =(elmaDeger + armutDeger).toShort()
                else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (elmaDeger+portakalDeger).toShort()
                else if(nesne.ikinciSayi=="seftali")
                        elmaDeger =(elmaDeger+ seftaliDeger).toShort()
                else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (elmaDeger+muzDeger).toShort()
                else
                    elmaDeger= (elmaDeger+nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        elmaDeger = (armutDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (armutDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (armutDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (armutDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (armutDeger + muzDeger).toShort()
                    else
                        elmaDeger = (armutDeger + nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (portakalDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (portakalDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (portakalDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (portakalDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (portakalDeger + muzDeger).toShort()
                    else
                        elmaDeger = (portakalDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (seftaliDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (seftaliDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (seftaliDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (seftaliDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (seftaliDeger + muzDeger).toShort()
                    else
                        elmaDeger = (seftaliDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        elmaDeger = (muzDeger + muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (muzDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (muzDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (muzDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger =  (muzDeger + portakalDeger).toShort()
                    else
                        elmaDeger = (muzDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else{
                    elmaDeger = (nesne.birinciSayi as Short + nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==2.toShort()){
                if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="elma")
                        elmaDeger =(elmaDeger- elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger =(elmaDeger- armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger =(elmaDeger- portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger =(elmaDeger- seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger =(elmaDeger- muzDeger).toShort()
                    else
                        elmaDeger= (elmaDeger-nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        elmaDeger = (armutDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (armutDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (armutDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (armutDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (armutDeger - muzDeger).toShort()
                    else
                        elmaDeger = (armutDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (portakalDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (portakalDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (portakalDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (portakalDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (portakalDeger - muzDeger).toShort()
                    else
                        elmaDeger = (portakalDeger- nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (seftaliDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (seftaliDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (seftaliDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (seftaliDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (seftaliDeger - muzDeger).toShort()
                    else
                        elmaDeger = (seftaliDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        elmaDeger = (muzDeger - muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (muzDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (muzDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (muzDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger =  (muzDeger - portakalDeger).toShort()
                    else
                        elmaDeger = (muzDeger - nesne.ikinciSayi as Short).toShort()
                }
                else{
                    elmaDeger = (nesne.birinciSayi as Short- nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==3.toShort()){
                if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="elma")
                        elmaDeger =(elmaDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger =(elmaDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger =(elmaDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger =(elmaDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger =(elmaDeger * muzDeger).toShort()
                    else
                        elmaDeger = (elmaDeger *nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        elmaDeger = (armutDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (armutDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (armutDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (armutDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (armutDeger * muzDeger).toShort()
                    else
                        elmaDeger = (armutDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (portakalDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (portakalDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (portakalDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (portakalDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (portakalDeger * muzDeger).toShort()
                    else
                        elmaDeger = (portakalDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (seftaliDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (seftaliDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (seftaliDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger = (seftaliDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        elmaDeger = (seftaliDeger * muzDeger).toShort()
                    else
                        elmaDeger = (seftaliDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        elmaDeger = (muzDeger * muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        elmaDeger = (muzDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        elmaDeger = (muzDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        elmaDeger = (muzDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        elmaDeger =  (muzDeger * portakalDeger).toShort()
                    else
                        elmaDeger = (muzDeger * nesne.ikinciSayi as Short).toShort()
                }
                else{
                    elmaDeger = (nesne.birinciSayi as Short * nesne.ikinciSayi as Short).toShort()
                }

            }
        }
        if(nesne.degisken==1.toShort()){
            if(nesne.islem==1.toShort()){
                if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        armutDeger =(armutDeger+ armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (armutDeger+elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (armutDeger+portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger =(armutDeger+ seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger =(armutDeger+ muzDeger).toShort()
                    else
                        armutDeger= (armutDeger+nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="armut")
                        armutDeger = (elmaDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (elmaDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (elmaDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (elmaDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (elmaDeger + muzDeger).toShort()
                    else
                        armutDeger = (elmaDeger + nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        armutDeger = (portakalDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (portakalDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (portakalDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (portakalDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (portakalDeger + muzDeger).toShort()
                    else
                        armutDeger = (portakalDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        armutDeger = (seftaliDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (seftaliDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (seftaliDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (seftaliDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (seftaliDeger + muzDeger).toShort()
                    else
                        armutDeger = (seftaliDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        armutDeger = (muzDeger + muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (muzDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (muzDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (muzDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger =  (muzDeger + portakalDeger).toShort()
                    else
                        armutDeger = (muzDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else{
                    armutDeger = (nesne.birinciSayi as Short+ nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==2.toShort()){
                if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="armut")
                        armutDeger = (elmaDeger-armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (elmaDeger-elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (elmaDeger-portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (elmaDeger-seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (elmaDeger-muzDeger).toShort()
                    else
                        armutDeger= (elmaDeger-nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        armutDeger = (armutDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (armutDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (armutDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (armutDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (armutDeger - muzDeger).toShort()
                    else
                        armutDeger = (armutDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        armutDeger = (portakalDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (portakalDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (portakalDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (portakalDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (portakalDeger - muzDeger).toShort()
                    else
                        armutDeger = (portakalDeger- nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        armutDeger = (seftaliDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (seftaliDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (seftaliDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (seftaliDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (seftaliDeger - muzDeger).toShort()
                    else
                        armutDeger = (seftaliDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        armutDeger = (muzDeger - muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (muzDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (muzDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (muzDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger =  (muzDeger - portakalDeger).toShort()
                    else
                        armutDeger = (muzDeger - nesne.ikinciSayi as Short).toShort()
                }
                else{
                    armutDeger = (nesne.birinciSayi as Short - nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==3.toShort()){
                if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        armutDeger = (armutDeger*armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (armutDeger*elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (armutDeger*portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (armutDeger*seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (armutDeger*muzDeger).toShort()
                    else
                        armutDeger = (armutDeger*nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="elma")
                        armutDeger = (elmaDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (elmaDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (elmaDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (elmaDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (elmaDeger * muzDeger).toShort()
                    else
                        armutDeger = (elmaDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        armutDeger = (portakalDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (portakalDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (portakalDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (portakalDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (portakalDeger * muzDeger).toShort()
                    else
                        armutDeger = (portakalDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        armutDeger = (seftaliDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (seftaliDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (seftaliDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger = (seftaliDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        armutDeger = (seftaliDeger * muzDeger).toShort()
                    else
                        armutDeger = (seftaliDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        armutDeger = (muzDeger * muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        armutDeger = (muzDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        armutDeger = (muzDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        armutDeger = (muzDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        armutDeger =  (muzDeger * portakalDeger).toShort()
                    else
                        armutDeger = (muzDeger * nesne.ikinciSayi as Short).toShort()
                }
                else{
                    armutDeger = (nesne.birinciSayi as Short * nesne.ikinciSayi as Short).toShort()
                }

            }
        }
        if(nesne.degisken==2.toShort()){
            if(nesne.islem==1.toShort()){
                if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        portakalDeger =(portakalDeger+ portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger =(portakalDeger+ elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger =(portakalDeger+ armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger =(portakalDeger+ seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger =(portakalDeger+ muzDeger).toShort()
                    else
                        portakalDeger= (portakalDeger+nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="armut")
                        portakalDeger = (elmaDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (elmaDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (elmaDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (elmaDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (elmaDeger + muzDeger).toShort()
                    else
                        portakalDeger = (elmaDeger + nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (armutDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (armutDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (armutDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (armutDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (armutDeger + muzDeger).toShort()
                    else
                        portakalDeger = (armutDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (seftaliDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (seftaliDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (seftaliDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (seftaliDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (seftaliDeger + muzDeger).toShort()
                    else
                        portakalDeger = (seftaliDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        portakalDeger = (muzDeger + muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (muzDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (muzDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (muzDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger =  (muzDeger + portakalDeger).toShort()
                    else
                        portakalDeger = (muzDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else{
                    portakalDeger = (nesne.birinciSayi as Short + nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==2.toShort()){
                if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        portakalDeger =(portakalDeger-  portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger =(portakalDeger-  elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger =(portakalDeger-  armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger =(portakalDeger-  seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger =(portakalDeger-  muzDeger).toShort()
                    else
                        portakalDeger=(portakalDeger- nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        portakalDeger = (armutDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (armutDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (armutDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (armutDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (armutDeger - muzDeger).toShort()
                    else
                        portakalDeger = (armutDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (elmaDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (elmaDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (elmaDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (elmaDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (elmaDeger - muzDeger).toShort()
                    else
                        portakalDeger = (elmaDeger- nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (seftaliDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (seftaliDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (seftaliDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (seftaliDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (seftaliDeger - muzDeger).toShort()
                    else
                        armutDeger = (seftaliDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        portakalDeger = (muzDeger - muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (muzDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (muzDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (muzDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger =  (muzDeger - portakalDeger).toShort()
                    else
                        portakalDeger = (muzDeger - nesne.ikinciSayi as Short).toShort()
                }
                else{
                    portakalDeger = (nesne.birinciSayi as Short - nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==3.toShort()){
                if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (portakalDeger*portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger =(portakalDeger* elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger =(portakalDeger* armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger =(portakalDeger* seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger =(portakalDeger* muzDeger).toShort()
                    else
                        portakalDeger =(portakalDeger* nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="elma")
                        portakalDeger = (elmaDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (elmaDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (elmaDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (elmaDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (elmaDeger * muzDeger).toShort()
                    else
                        portakalDeger = (elmaDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (armutDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (armutDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (armutDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (armutDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (armutDeger * muzDeger).toShort()
                    else
                        portakalDeger = (armutDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (seftaliDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (seftaliDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (seftaliDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger = (seftaliDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        portakalDeger = (seftaliDeger * muzDeger).toShort()
                    else
                        portakalDeger = (seftaliDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        portakalDeger = (muzDeger * muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        portakalDeger = (muzDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        portakalDeger = (muzDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        portakalDeger = (muzDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        portakalDeger =  (muzDeger * portakalDeger).toShort()
                    else
                        portakalDeger = (muzDeger * nesne.ikinciSayi as Short).toShort()
                }
                else{
                    portakalDeger = (nesne.birinciSayi as Short * nesne.ikinciSayi as Short).toShort()
                }

            }
        }
        if(nesne.degisken==3.toShort()){
            if(nesne.islem==1.toShort()){
                if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="armut")
                        seftaliDeger =(seftaliDeger+ armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger =(seftaliDeger+ elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (seftaliDeger+portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger =(seftaliDeger+ seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger =(seftaliDeger+ muzDeger).toShort()
                    else
                        seftaliDeger= (seftaliDeger+nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (elmaDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (elmaDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (elmaDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (elmaDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (elmaDeger + muzDeger).toShort()
                    else
                        seftaliDeger = (elmaDeger + nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (portakalDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (portakalDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (portakalDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (portakalDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (portakalDeger + muzDeger).toShort()
                    else
                        seftaliDeger = (portakalDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (armutDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (armutDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (armutDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (armutDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (armutDeger + muzDeger).toShort()
                    else
                        seftaliDeger = (armutDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (muzDeger + muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (muzDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (muzDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (muzDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger =  (muzDeger + portakalDeger).toShort()
                    else
                        seftaliDeger = (muzDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else{
                    seftaliDeger = (nesne.birinciSayi as Short + nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==2.toShort()){
                if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="armut")
                        seftaliDeger =(seftaliDeger-  armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger =(seftaliDeger-  elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger =(seftaliDeger-  portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger =(seftaliDeger-  seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger =(seftaliDeger-  muzDeger).toShort()
                    else
                        seftaliDeger=(seftaliDeger- nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (elmaDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (elmaDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (elmaDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (elmaDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (elmaDeger - muzDeger).toShort()
                    else
                        seftaliDeger = (elmaDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (portakalDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (portakalDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (portakalDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (portakalDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (portakalDeger - muzDeger).toShort()
                    else
                        seftaliDeger = (portakalDeger- nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (armutDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (armutDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (armutDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (armutDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (armutDeger - muzDeger).toShort()
                    else
                        seftaliDeger = (armutDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (muzDeger - muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (muzDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (muzDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (muzDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger =  (muzDeger - portakalDeger).toShort()
                    else
                        seftaliDeger = (muzDeger - nesne.ikinciSayi as Short).toShort()
                }
                else{
                    seftaliDeger = (nesne.birinciSayi as Short - nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==3.toShort()){
                if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (seftaliDeger*armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger =(seftaliDeger* elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger =(seftaliDeger* portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger =(seftaliDeger* seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger =(seftaliDeger* muzDeger).toShort()
                    else
                        seftaliDeger =(seftaliDeger*  nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (elmaDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (elmaDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (elmaDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (elmaDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (elmaDeger * muzDeger).toShort()
                    else
                        seftaliDeger = (elmaDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (portakalDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (portakalDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (portakalDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (portakalDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (portakalDeger * muzDeger).toShort()
                    else
                        seftaliDeger = (portakalDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (armutDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (armutDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (armutDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger = (armutDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (armutDeger * muzDeger).toShort()
                    else
                        seftaliDeger = (armutDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="muz")
                        seftaliDeger = (muzDeger * muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        seftaliDeger = (muzDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        seftaliDeger = (muzDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        seftaliDeger = (muzDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        seftaliDeger =  (muzDeger * portakalDeger).toShort()
                    else
                        seftaliDeger = (muzDeger * nesne.ikinciSayi as Short).toShort()
                }
                else{
                    seftaliDeger = (nesne.birinciSayi as Short * nesne.ikinciSayi as Short).toShort()
                }

            }
        }
        if(nesne.degisken==4.toShort()){
            if(nesne.islem==1.toShort()){
                if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="armut")
                        muzDeger =(muzDeger+  armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger =(muzDeger+  elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger =(muzDeger+  portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (muzDeger+ seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger =(muzDeger+  muzDeger).toShort()
                    else
                        muzDeger=(muzDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="armut")
                        muzDeger = (elmaDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (elmaDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (elmaDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (elmaDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (elmaDeger + muzDeger).toShort()
                    else
                        muzDeger = (elmaDeger + nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        muzDeger = (portakalDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (portakalDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (portakalDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (portakalDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (portakalDeger + muzDeger).toShort()
                    else
                        muzDeger = (portakalDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        muzDeger = (seftaliDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (seftaliDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (seftaliDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (seftaliDeger + portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (seftaliDeger + muzDeger).toShort()
                    else
                        muzDeger = (seftaliDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="muz")
                        muzDeger = (armutDeger + muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (armutDeger + elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (armutDeger + armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (armutDeger + seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger =  (armutDeger + portakalDeger).toShort()
                    else
                        muzDeger = (armutDeger+ nesne.ikinciSayi as Short).toShort()
                }
                else{
                    muzDeger = (nesne.birinciSayi as Short + nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==2.toShort()){
                if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="armut")
                        muzDeger = (muzDeger-armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (muzDeger-elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (muzDeger-portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (muzDeger-seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (muzDeger-muzDeger).toShort()
                    else
                        muzDeger= (muzDeger-nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="armut")
                        muzDeger = (armutDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (armutDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (armutDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (armutDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (armutDeger - muzDeger).toShort()
                    else
                        muzDeger = (armutDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        muzDeger = (portakalDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (portakalDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (portakalDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (portakalDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (portakalDeger - muzDeger).toShort()
                    else
                        muzDeger = (portakalDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        muzDeger = (seftaliDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (seftaliDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (seftaliDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (seftaliDeger - portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (seftaliDeger - muzDeger).toShort()
                    else
                        muzDeger = (seftaliDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="muz")
                        muzDeger = (elmaDeger - muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (elmaDeger - elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (elmaDeger - armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (elmaDeger - seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger =  (elmaDeger - portakalDeger).toShort()
                    else
                        muzDeger = (elmaDeger - nesne.ikinciSayi as Short).toShort()
                }
                else{
                    muzDeger = (nesne.birinciSayi as Short - nesne.ikinciSayi as Short).toShort()
                }
            }
            if(nesne.islem==3.toShort()){
                if(nesne.birinciSayi=="muz"){
                    if(nesne.ikinciSayi=="armut")
                        muzDeger = (muzDeger*armutDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (muzDeger*elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (muzDeger*portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (muzDeger*seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (muzDeger*muzDeger).toShort()
                    else
                        muzDeger = (muzDeger*nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="elma"){
                    if(nesne.ikinciSayi=="elma")
                        muzDeger = (elmaDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (elmaDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (elmaDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (elmaDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (elmaDeger * muzDeger).toShort()
                    else
                        muzDeger = (elmaDeger - nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="portakal"){
                    if(nesne.ikinciSayi=="portakal")
                        muzDeger = (portakalDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (portakalDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (portakalDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (portakalDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (portakalDeger * muzDeger).toShort()
                    else
                        muzDeger = (portakalDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="seftali"){
                    if(nesne.ikinciSayi=="seftali")
                        muzDeger = (seftaliDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (seftaliDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (seftaliDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger = (seftaliDeger * portakalDeger).toShort()
                    else if(nesne.ikinciSayi=="muz")
                        muzDeger = (seftaliDeger * muzDeger).toShort()
                    else
                        muzDeger = (seftaliDeger * nesne.ikinciSayi as Short).toShort()
                }
                else if(nesne.birinciSayi=="armut"){
                    if(nesne.ikinciSayi=="muz")
                        muzDeger = (armutDeger * muzDeger).toShort()
                    else if(nesne.ikinciSayi=="elma")
                        muzDeger = (armutDeger * elmaDeger).toShort()
                    else if(nesne.ikinciSayi=="armut")
                        muzDeger = (armutDeger * armutDeger).toShort()
                    else if(nesne.ikinciSayi=="seftali")
                        muzDeger = (armutDeger * seftaliDeger).toShort()
                    else if(nesne.ikinciSayi=="portakal")
                        muzDeger =  (armutDeger * portakalDeger).toShort()
                    else
                        muzDeger = (armutDeger * nesne.ikinciSayi as Short).toShort()
                }
                else{
                    muzDeger = (nesne.birinciSayi as Short * nesne.ikinciSayi as Short).toShort()
                }

            }
        }

    }
    private fun sartKontrol(sartId:Short?):Boolean {
        if(sartId==null)
            return false
        else{
            var sonuc=true
            val i=nesneListesi.find { x -> x.nesneId == sartId  }
            if(i!!.nesneTuru==4.toShort()&&i.kod=="adet")
            {
                if(i.adet>0) {
                    i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                    i.adet--
                    i.degisken++
                    return true
                }
                else{
                    i.adet= i.degisken
                    i.degisken=0
                    return false
                }
            }
            else if(i.nesneTuru==4.toShort()&&i.kod=="deg")
            {
                if(i.degisken==0.toShort()) {
                    if(elmaDeger>0){
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        if(i.adet< elmaDeger){
                            i.adet++
                            return true
                        }
                        else{
                            i.adet=0
                            return false
                        }
                    }
                    else{
                        return false
                    }
                }
                if(i.degisken==1.toShort()) {
                    if(armutDeger>0){
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        if(i.adet< armutDeger){
                            i.adet++
                            return true
                        }
                        else{
                            i.adet=0
                            return false
                        }
                    }
                    else{
                        return false
                    }
                }
                if(i.degisken==2.toShort()){
                    if(portakalDeger>0){
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        if(i.adet< portakalDeger){
                            i.adet++
                            return true
                        }
                        else{
                            i.adet=0
                            return false
                        }
                    }
                    else{
                        return false
                    }
                }
                if(i.degisken==3.toShort()) {
                    if(seftaliDeger>0){
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        if(i.adet< seftaliDeger){
                            i.adet++
                            return true
                        }
                        else{
                            i.adet=0
                            return false
                        }
                    }
                    else{
                        return false
                    }
                }
                if(i.degisken==4.toShort()) {
                    if(muzDeger>0){
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        if(i.adet< muzDeger){
                            i.adet++
                            return true
                        }
                        else{
                            i.adet=0
                            return false
                        }
                    }
                    else{
                        return false
                    }
                }
            }
            else if(i.nesneTuru==4.toShort()&&i.kod=="karsilastirdd")
            {
                var anaDegTemp:Short=elmaDeger
                var karsilastirilanDegisken:Short=elmaDeger

                if(i.degisken==0.toShort())
                anaDegTemp= elmaDeger
                else if(i.degisken==1.toShort())
                    anaDegTemp= armutDeger
                else if(i.degisken==2.toShort())
                    anaDegTemp= portakalDeger
                else if(i.degisken==3.toShort())
                    anaDegTemp= seftaliDeger
                else if(i.degisken==4.toShort())
                    anaDegTemp= muzDeger
                if(i.birinciSayi==0.toShort())
                    karsilastirilanDegisken=elmaDeger
                else if(i.birinciSayi==1.toShort())
                    karsilastirilanDegisken=armutDeger
                else if(i.birinciSayi==2.toShort())
                    karsilastirilanDegisken=portakalDeger
                else if(i.birinciSayi==3.toShort())
                    karsilastirilanDegisken=seftaliDeger
                else if(i.birinciSayi==4.toShort())
                    karsilastirilanDegisken=muzDeger
                if(i.karsilastirmaOperatoru==1.toShort())
                    sonuc=!(i.istek.xor (anaDegTemp==karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==2.toShort())
                    sonuc=!(i.istek.xor (anaDegTemp!=karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==3.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp>karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==4.toShort())
                    sonuc=!(i.istek.xor (anaDegTemp>=karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==5.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp<karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==6.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp<=karsilastirilanDegisken))
                //return true
            }
            else if(i.nesneTuru==4.toShort()&&i.kod=="karsilastirds")
            {
                var anaDegTemp:Short=elmaDeger
                var karsilastirilanDegisken:Short=i.secim
                if(i.degisken==0.toShort())
                    anaDegTemp= elmaDeger
                else if(i.degisken==1.toShort())
                    anaDegTemp= armutDeger
                else if(i.degisken==2.toShort())
                    anaDegTemp= portakalDeger
                else if(i.degisken==3.toShort())
                    anaDegTemp= seftaliDeger
                else if(i.degisken==4.toShort())
                    anaDegTemp= muzDeger
                if(i.karsilastirmaOperatoru==1.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp==karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==2.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp!=karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==3.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp>karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==4.toShort())
                    sonuc=!(i.istek.xor (anaDegTemp>=karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==5.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp<karsilastirilanDegisken))
                else if(i.karsilastirmaOperatoru==6.toShort())
                    sonuc=!(i.istek.xor(anaDegTemp<=karsilastirilanDegisken))
            }
            else if(i.nesneTuru==2.toShort()){
                if (mBluetoothSocket != null) {
                    try{
                        mBluetoothSocket!!.outputStream.write(i.kod.toByteArray())
                        i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
                        sonuc =true
                    } catch(e: IOException) {
                        e.printStackTrace()
                        sonuc=false
                    }
                }
            }
            if(sonuc)
                i.nesneResim.isik.setImageResource(R.drawable.yesilisik)
            return sonuc
        }
    }
    private fun komutGonder(input: String) : Boolean {
        var kontrol =true
        string=""
        if (mBluetoothSocket != null) {
            try{
                mBluetoothSocket!!.outputStream.write(input.toByteArray())
                //nesne.nesneResim.isik.setImageResource
                kontrol= true
            } catch(e: IOException) {
                e.printStackTrace()
                kontrol= false
            }
        }
        return kontrol
    }
    private fun disconnect() {
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket!!.close()
                mBluetoothSocket = null
                mIsConnected = false
                connectionImage.setImageResource(R.drawable.disconnected)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private val mDeviceClickListener = AdapterView.OnItemClickListener { av, v, arg2, arg3 ->
        // Cancel discovery because it's costly and we're about to connect
        pencereBtBaglanti.visibility = INVISIBLE
        progressBarBt.bringToFront()
        mBluetoothAdapter.cancelDiscovery()
        // Get the device MAC address, which is the last 17 chars in the View
        val info = (v.textCihaz as TextView).text.toString()
        val address = info.substring(info.length - 17)
        BtConnection(address).execute()
    }
    private fun uzatilacakNesneleriUzat(id:Short?,ekleDifY: Int,view2Height:Int,say:Int) {
        if(id==null||id==0.toShort())
            return
        //var j=nesneListesi.find { x -> x.nesneId == id  }
        //uzatilacakNesneleriUzat(Nesne.id.toShort(),ekleDifY.toInt(),view2.height/2,1)
        for (j in nesneListesi) {
            //geciciViewListesi3.remove(j.nesneAltId)
            if (j.nesneIcId == id) {
                //geciciViewListesi3.remove(j.nesneAltId)
                if (say==0||say==2) {//boş yere giriyorsam
                    j.nesneResim.layoutParams.height += ekleDifY-ileritek.height+view2Height
                    uzatilacakNesneleriUzat(j.nesneId,ekleDifY,view2Height,say)
                    nesneleriAsagiTasi(j.nesneAltId,ekleDifY-ileritek.height+view2Height)
                }
                else {
                    //geciciViewListesi3.remove(j.nesneAltId)
                    j.nesneResim.layoutParams.height += ekleDifY //abs(enAlttakiCocukBottom(id) - j.nesneResim.y.toInt()) + ileritek.height / 2
                    uzatilacakNesneleriUzat(j.nesneId,ekleDifY,view2Height,1)
                    nesneleriAsagiTasi(j.nesneAltId,ekleDifY)
                }
            }
            else if (j.nesneAltId ==id){
                if(say==2){
                    uzatilacakNesneleriUzat(j.nesneId,ekleDifY,view2Height,say)
                }
                else{
                    uzatilacakNesneleriUzat(j.nesneId,ekleDifY,view2Height,1)
                }
            }
        }
    }
    private fun kisaltilacakNesneleriKisalt(id: Short?,difY:Int,view2Height:Int,say:Int) {
        if(id==null||id==0.toShort())
            return
        for (j in nesneListesi) {
            if (j.nesneIcId == id) {
                if (say==0||say==2){//ben çıktığımda boş kalacaksa
                    geciciViewListesi3.remove(j.nesneAltId)
                    j.nesneResim.layoutParams.height -= (difY-ileritek.height+view2Height)
                    kisaltilacakNesneleriKisalt(j.nesneId,difY,view2Height,say)
                    nesneleriYukariTasi(j.nesneAltId,difY-ileritek.height+view2Height)
                }
                else{//ben çıktığımda boş kalmayacaksa
                    geciciViewListesi3.remove(j.nesneAltId)
                    j.nesneResim.layoutParams.height -= difY
                    kisaltilacakNesneleriKisalt(j.nesneId,difY,view2Height,1)
                    nesneleriYukariTasi(j.nesneAltId,difY)
                }
            }
            else if (j.nesneAltId ==id){//içerde değilsem
                if(say==2){
                    kisaltilacakNesneleriKisalt(j.nesneId,difY,view2Height,say)
                    //nesneleriYukariTasi(j.nesneAltId,difY)
                }
                else{
                    kisaltilacakNesneleriKisalt(j.nesneId,difY,view2Height,1)
                    //nesneleriYukariTasi(j.nesneAltId,difY)
                }

            }
        }
    }
    private fun nesneleriYukariTasi(nesneAltId: Short?, difY: Int) {
        geciciViewListesiYap3(nesneAltId)
        for(id in geciciViewListesi3)
        {
            val i = nesneListesi.find { x -> x.nesneId == id }
            if(!kaydirilanlar.contains(i!!.nesneId)) {
                i.nesneResim.y -= difY
                kaydirilanlar.add(i.nesneId)
            }
        }
    }
    private fun nesneleriAsagiTasi(nesneAltId: Short?, difY: Int) {
        //geciciViewListesi3.clear()
        geciciViewListesiYap3(nesneAltId)
        for(id in geciciViewListesi3)
        {
            val i = nesneListesi.find { x -> x.nesneId == id }
            if(!kaydirilanlar.contains(i!!.nesneId)) {
                i.nesneResim.y += difY
                kaydirilanlar.add(i.nesneId)
            }
            }
    }
    private fun enAlttakiCocukBottom(id: Short):Int {
        var bottom=0
        for (i in nesneListesi) {
            if (id == i.nesneId) {
                if (i.nesneAltId == null) {
                    bottom= i.nesneResim.y.toInt()+i.nesneResim.height
                }
                else
                    bottom  = enAlttakiCocukBottom(i.nesneAltId!!)
            }
        }
        return bottom
    }
    private fun enAlttakiCocukBul(id: Short):Short {
       var geciciId:Short=id
       for (i in nesneListesi) {
            if (id == i.nesneId) {
                if (i.nesneAltId != null)
                    geciciId = enAlttakiCocukBul(i.nesneAltId!!)
            }
        }
        return geciciId
   }
    private fun eklendigimYerdekiCocugunParentiniBenYap(id: Short?, enAlttakiCocukBul: Short) {
        if (id == null) {
            return
        }
        for (i in nesneListesi) {
            if (i.nesneId == id) {
                i.parent = enAlttakiCocukBul
                for (j in nesneListesi) {
                    if (j.nesneId == enAlttakiCocukBul) {
                        j.nesneAltId = id
                        if (j.active) {
                            tumCocuklariAktifYap(i.nesneId)
                        } else {
                            tumCocuklariPasifYap(i.nesneId)
                        }
                    }
                }
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    inner class BtConnection (var address: String): AsyncTask<String, String, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBarBt.visibility=View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String? {
            try {

                if (mBluetoothSocket == null || !mIsConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = mBluetoothAdapter.getRemoteDevice(address)
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(mUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    mBluetoothSocket!!.connect()
                    //thread= ConnectedThread(mBluetoothSocket)
                    mIsConnected=true
                    //mHandler.postDelayed(mRunnable, 1000)

                }

            } catch (e: IOException) {
                mIsConnected=false
                e.printStackTrace()
            }
            return mIsConnected.toString()
        }
        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            progressBarBt.visibility=View.INVISIBLE
            if(s=="true") {
                Toast.makeText(this@FullscreenActivity, R.string.basariylabaglanildi, Toast.LENGTH_SHORT).show()
                connectionImage.setImageResource(R.drawable.connected)
            }
            else
            Toast.makeText(this@FullscreenActivity, R.string.baglantihatasi, Toast.LENGTH_LONG).show()

        }
    }
}
