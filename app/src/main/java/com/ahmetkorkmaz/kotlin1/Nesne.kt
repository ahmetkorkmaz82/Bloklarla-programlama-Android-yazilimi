package com.ahmetkorkmaz.kotlin1

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.adetdeg.view.*
import kotlinx.android.synthetic.main.deg_islem_deg.view.*
import kotlinx.android.synthetic.main.deg_islem_sayi.view.*
import kotlinx.android.synthetic.main.elma.view.*
import kotlinx.android.synthetic.main.adet.view.*
import kotlinx.android.synthetic.main.fonksiyon.view.*
import kotlinx.android.synthetic.main.sart1.view.*
import kotlinx.android.synthetic.main.sart2.view.*
import kotlinx.android.synthetic.main.sart3.view.*
import kotlinx.android.synthetic.main.sart_cizgi.view.*
import kotlinx.android.synthetic.main.sart_engel.view.*
import kotlinx.android.synthetic.main.sartdegtodeg.view.*
import kotlinx.android.synthetic.main.sayi_islem_sayi.view.*
import kotlinx.android.synthetic.main.sartdegtosayi.view.*
import kotlinx.android.synthetic.main.renk.view.*
import kotlin.coroutines.coroutineContext

data class Nesne(var nesneResim:View, val nesneTuru:Short=0, var kod:String, val nesneId:Short, var active:Boolean=false) {
    //nesneTuru=0:komut, 1:kontrol, 2:koşul, 3:while döngü, 4: adet 5: matematik 6:fonksiyon 9:basla10:Değişken
    var parent: Short? = null
    //var active=false
    var yapisibil = true
    var nesneFonkId: Short? = null
    var nesneSartId: Short? = null
    var nesneAltId: Short? = null
    var nesneIcId: Short? = null
    var karsilastirmaOperatoru:Short=0
    var istek: Boolean = true
    var degisken: Short = 0//0:Elma, 1: Armut, 2:Portakal, 3:Şeftali, 4:Muz
    var birinciSayi: Any = 0
    var adet: Short = 0
    var ikinciSayi: Any = 0
    var islem: Short = 1//1:Toplama,2:Çıkartma,3:Çarpma,4:Bölme
    var secim: Short = 7//0:Boş 1:Kırmızı 2:Yeşil 3:Mavi 4:Çizgi Önde 5:Çizgi Sağda 6:Çizgi Solda 7: Engel önde 8:Engel Sağda 9:Engel Solda

    constructor(nesneResim: View?, nesneTuru: Short, kod: String, nesneId: Short, active: Boolean, ad: String) : this(nesneResim!!, nesneTuru, kod, nesneId, active = false) {
        nesneResim.txtFonksiyon.text = ad
    }
    //fonk oluşturma sorun yok
    constructor(nesneResim: View?, nesneTuru: Short, kod: String, nesneId: Short, deger: Any, degisken: Short) : this(nesneResim!!, nesneTuru, kod, nesneId, active = false) {
        if (degisken == 0.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.elma)
        }
        else if (degisken == 1.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.armut)
        }
        else if (degisken == 2.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.portakal)
        }
        else if (degisken == 3.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.seftali)
        }
        else if (degisken == 4.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.muz)
        }
        nesneResim.degText.text = deger.toString()
        birinciSayi = deger
        islem = 1.toShort()
        ikinciSayi = 0.toShort()
        this.degisken = degisken
    }
    //değişken oluşturma sorun yok
    constructor(nesneResim: View?, nesneTuru: Short, kod: String, nesneId: Short, birinciDeger: Short, islem: Short, ikinciDeger: Any, degisken: Short, active: Boolean) : this(nesneResim!!, nesneTuru, kod, nesneId, active = false) {
        var iki: Short = ikinciDeger as Short
        if (degisken == 0.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.elma)

        }
        if (degisken == 1.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.armut)
        }
        if (degisken == 2.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.portakal)
        }
        if (degisken == 3.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.seftali)
        }
        if (degisken == 4.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.muz)
        }
        if (birinciDeger == 0.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.elma)
            birinciSayi = "elma"
        }
        if (birinciDeger == 1.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.armut)
            birinciSayi = "armut"
        }
        if (birinciDeger == 2.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.portakal)
            birinciSayi = "portakal"
        }
        if (birinciDeger == 3.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.seftali)
            birinciSayi = "seftali"
        }
        if (birinciDeger == 4.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.muz)
            birinciSayi = "muz"
        }
        if (islem == 1.toShort()) {
            nesneResim.isaretDegToDeg.setImageResource(R.drawable.arti)
        }
        if (islem == 2.toShort()) {
            nesneResim.isaretDegToDeg.setImageResource(R.drawable.eksi)
        }
        if (islem == 3.toShort()) {
            nesneResim.isaretDegToDeg.setImageResource(R.drawable.carp)
        }
        if (iki == 0.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.elma)
            ikinciSayi = "elma"
        }
        if (iki == 1.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.armut)
            ikinciSayi = "armut"
        }
        if (iki == 2.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.portakal)
            ikinciSayi = "portakal"
        }
        if (iki == 3.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.seftali)
            ikinciSayi = "seftali"

        }
        if (iki == 4.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.muz)
            ikinciSayi = "muz"

        }
        this.degisken = degisken
        this.islem = islem
        //this.ikinciSayi= iki
        //this.birinciSayi=birinciDeger

    }
    //değişken işlem değişken sorun yok
    constructor(nesneResim: View?, nesneTuru: Short, kod: String, nesneId: Short, birinciDeger: Short, ikinciDeger: Short, islem: Short, degisken: Short) : this(nesneResim!!, nesneTuru, kod, nesneId, active = false) {
        if (islem == 1.toShort())
            nesneResim.isaretIslemToSayi.setImageResource(R.drawable.arti)
        if (islem == 2.toShort())
            nesneResim.isaretIslemToSayi.setImageResource(R.drawable.eksi)
        if (islem == 3.toShort())
            nesneResim.isaretIslemToSayi.setImageResource(R.drawable.carp)
        if (degisken == 0.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.elma)
        } else if (degisken == 1.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.armut)
        } else if (degisken == 2.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.portakal)
        } else if (degisken == 3.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.seftali)
        } else if (degisken == 4.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.muz)
        }
        if (birinciDeger == 0.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.elma)
            birinciSayi = "elma"
        } else if (birinciDeger == 1.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.armut)
            birinciSayi = "armut"
        } else if (birinciDeger == 2.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.portakal)
            birinciSayi = "portakal"
        } else if (birinciDeger == 3.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.seftali)
            birinciSayi = "seftali"
        } else if (birinciDeger == 4.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.muz)
            birinciSayi = "muz"
        }
        nesneResim.degTextIslemToSayi.text = ikinciDeger.toString()
        this.degisken = degisken
        this.islem = islem
        ikinciSayi = ikinciDeger
    }
    //değişken işlem sayı sorun yok
    constructor(nesneResim: View?, nesneTuru: Short, kod: String, nesneId: Short, active: Boolean, birinciSayi: Short, ikinciSayi: Short, islem: Short, degisken: Short) : this(nesneResim!!, nesneTuru, kod, nesneId, active = false) {
        this.active = active
        if (islem == 1.toShort())
            nesneResim.isaretsayitosayi.setImageResource(R.drawable.arti)
        if (islem == 2.toShort())
            nesneResim.isaretsayitosayi.setImageResource(R.drawable.eksi)
        if (islem == 3.toShort())
            nesneResim.isaretsayitosayi.setImageResource(R.drawable.carp)
        if (degisken == 0.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.elma)
        } else if (degisken == 1.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.armut)
        } else if (degisken == 2.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.portakal)
        } else if (degisken == 3.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.seftali)
        } else if (degisken == 4.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.muz)
        }


        nesneResim.degTextsayitosayi.text = birinciSayi.toString()
        nesneResim.degText2sayitosayi.text = ikinciSayi.toString()
        this.degisken = degisken
        this.islem = islem
        this.ikinciSayi = ikinciSayi
        this.birinciSayi = birinciSayi
        //this.nesneResim=nesneResim
    }
    //sayı işlem sayı sorun yok
    constructor(nesneResim: View, nesneTuru: Short = 0, kod: String, nesneId: Short, active: Boolean = false, degisken: Short) : this(nesneResim, nesneTuru, kod, nesneId, active = false) {
        this.active = active
        if (degisken == 0.toShort()) {
            nesneResim.deg1.setImageResource(R.drawable.elma)
        } else if (degisken == 1.toShort()) {
            nesneResim.deg1.setImageResource(R.drawable.armut)
        } else if (degisken == 2.toShort()) {
            nesneResim.deg1.setImageResource(R.drawable.portakal)
        } else if (degisken == 3.toShort()) {
            nesneResim.deg1.setImageResource(R.drawable.seftali)
        } else if (degisken == 4.toShort()) {
            nesneResim.deg1.setImageResource(R.drawable.muz)
        }

        this.degisken = degisken

    }
    //değişken adet sorun yok
    constructor(nesneResim: View, nesneTuru: Short = 0, kod: String, nesneId: Short, adet: Short) : this(nesneResim, nesneTuru, kod, nesneId, active = false) {
        this.adet = adet
        nesneResim.defaShortToText.text = adet.toString()
    }
    //sayı adet sorun yok
    constructor(nesneResim: View, nesneTuru: Short = 0,kod:String, istek: Boolean, nesneId: Short) : this(nesneResim, nesneTuru, kod, nesneId, active = false) {
        this.istek=istek
    }
    //koşul kontrol oluşturma sorun yok
    constructor(nesneResim: View, nesneTuru: Short = 0,kod:String,nesneId: Short,degiskenGelen: Short, karsilastirmaOperatoruGelen:Short,birinciDeger: Short) : this(nesneResim, nesneTuru, kod, nesneId, active = false) {
        if(kod.equals("karsilastirds")) {
        karsilastirmaOperatoru = karsilastirmaOperatoruGelen
        degisken = degiskenGelen
        secim = birinciDeger

        if (degiskenGelen == 0.toShort()) {
                nesneResim.anaDegToSayi.setImageResource(R.drawable.elma)
            } else if (degiskenGelen == 1.toShort()) {
                nesneResim.anaDegToSayi.setImageResource(R.drawable.armut)
            } else if (degiskenGelen == 2.toShort()) {
                nesneResim.anaDegToSayi.setImageResource(R.drawable.portakal)
            } else if (degiskenGelen == 3.toShort()) {
                nesneResim.anaDegToSayi.setImageResource(R.drawable.seftali)
            } else if (degiskenGelen == 4.toShort()) {
                nesneResim.anaDegToSayi.setImageResource(R.drawable.muz)
            }

            if (karsilastirmaOperatoruGelen == 1.toShort())
                nesneResim.signdegtosayi.setImageResource(R.drawable.esit)
            else if (karsilastirmaOperatoruGelen == 2.toShort())
                nesneResim.signdegtosayi.setImageResource(R.drawable.esitdegil)
            else if (karsilastirmaOperatoruGelen == 3.toShort())
                nesneResim.signdegtosayi.setImageResource(R.drawable.buyuktur)
            else if (karsilastirmaOperatoruGelen == 4.toShort())
                nesneResim.signdegtosayi.setImageResource(R.drawable.buyukesit)
            else if (karsilastirmaOperatoruGelen == 5.toShort())
                nesneResim.signdegtosayi.setImageResource(R.drawable.kucuktur)
            else if (karsilastirmaOperatoruGelen == 6.toShort())
                nesneResim.signdegtosayi.setImageResource(R.drawable.kucukesit)

            nesneResim.txDegSayiKarsilastir.text = birinciDeger.toString()

        }
        else if(kod.equals("karsilastirdd")) {
            karsilastirmaOperatoru = karsilastirmaOperatoruGelen
            degisken = degiskenGelen
            birinciSayi = birinciDeger
            if (degiskenGelen == 0.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.elma)
            } else if (degiskenGelen == 1.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.armut)
            } else if (degiskenGelen == 2.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.portakal)
            } else if (degiskenGelen == 3.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.seftali)
            } else if (degiskenGelen == 4.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.muz)
            }
            if (karsilastirmaOperatoruGelen == 1.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.esit)
            else if (karsilastirmaOperatoruGelen == 2.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.esitdegil)
            else if (karsilastirmaOperatoruGelen == 3.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.buyuktur)
            else if (karsilastirmaOperatoruGelen == 4.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.buyukesit)
            else if (karsilastirmaOperatoruGelen == 5.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.kucuktur)
            else if (karsilastirmaOperatoruGelen == 6.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.kucukesit)

            if (birinciDeger == 0.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.elma)
            } else if (birinciDeger == 1.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.armut)
            } else if (birinciDeger == 2.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.portakal)
            } else if (birinciDeger == 3.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.seftali)
            } else if (birinciDeger == 4.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.muz)
            }
        }

    }
    //değişken sayı karşılaştır sorun yok
    fun degiskenSayiKarsilastirDuzenle(degiskenGelen: Short, karsilastirmaOperatoruGelen:Short,birinciDeger: Short){
        karsilastirmaOperatoru = karsilastirmaOperatoruGelen
        degisken = degiskenGelen
        secim = birinciDeger
        if (degiskenGelen == 0.toShort()) {
            nesneResim.anaDegToSayi.setImageResource(R.drawable.elma)
        } else if (degiskenGelen == 1.toShort()) {
            nesneResim.anaDegToSayi.setImageResource(R.drawable.armut)
        } else if (degiskenGelen == 2.toShort()) {
            nesneResim.anaDegToSayi.setImageResource(R.drawable.portakal)
        } else if (degiskenGelen == 3.toShort()) {
            nesneResim.anaDegToSayi.setImageResource(R.drawable.seftali)
        } else if (degiskenGelen == 4.toShort()) {
            nesneResim.anaDegToSayi.setImageResource(R.drawable.muz)
        }
        if (karsilastirmaOperatoruGelen == 1.toShort())
            nesneResim.signdegtosayi.setImageResource(R.drawable.esit)
        else if (karsilastirmaOperatoruGelen == 2.toShort())
            nesneResim.signdegtosayi.setImageResource(R.drawable.esitdegil)
        else if (karsilastirmaOperatoruGelen == 3.toShort())
            nesneResim.signdegtosayi.setImageResource(R.drawable.buyuktur)
        else if (karsilastirmaOperatoruGelen == 4.toShort())
            nesneResim.signdegtosayi.setImageResource(R.drawable.buyukesit)
        else if (karsilastirmaOperatoruGelen == 5.toShort())
            nesneResim.signdegtosayi.setImageResource(R.drawable.kucuktur)
        else if (karsilastirmaOperatoruGelen == 6.toShort())
            nesneResim.signdegtosayi.setImageResource(R.drawable.kucukesit)
        nesneResim.txDegSayiKarsilastir.text = birinciDeger.toString()
    }
    fun degiskenDegiskenKarsilastirDuzenle(degiskenGelen: Short, karsilastirmaOperatoruGelen:Short,birinciDeger: Short){
        karsilastirmaOperatoru = karsilastirmaOperatoruGelen
        degisken = degiskenGelen
        birinciSayi = birinciDeger

        if (degiskenGelen == 0.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.elma)
            } else if (degiskenGelen == 1.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.armut)
            } else if (degiskenGelen == 2.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.portakal)
            } else if (degiskenGelen == 3.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.seftali)
            } else if (degiskenGelen == 4.toShort()) {
                nesneResim.anaDegSartDegToDeg.setImageResource(R.drawable.muz)
            }
            if (karsilastirmaOperatoruGelen == 1.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.esit)
            else if (karsilastirmaOperatoruGelen == 2.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.esitdegil)
            else if (karsilastirmaOperatoruGelen == 3.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.buyuktur)
            else if (karsilastirmaOperatoruGelen == 4.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.buyukesit)
            else if (karsilastirmaOperatoruGelen == 5.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.kucuktur)
            else if (karsilastirmaOperatoruGelen == 6.toShort())
                nesneResim.signdegtodeg.setImageResource(R.drawable.kucukesit)

            if (birinciDeger == 0.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.elma)
            } else if (birinciDeger == 1.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.armut)
            } else if (birinciDeger == 2.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.portakal)
            } else if (birinciDeger == 3.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.seftali)
            } else if (birinciDeger == 4.toShort()) {
                nesneResim.anaDegSartDegToDeg2.setImageResource(R.drawable.muz)
            }

    }
    fun sayiAdetDuzenle(adet:Short){
        this.adet = adet
        nesneResim.defaShortToText.text = adet.toString()
    }
    fun degiskenDuzenle(deger: Any, degisken: Short){
        if (degisken == 0.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.elma)
        }
        else if (degisken == 1.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.armut)
        }
        else if (degisken == 2.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.portakal)
        }
        else if (degisken == 3.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.seftali)
        }
        else if (degisken == 4.toShort()) {
            nesneResim.elmasign.setImageResource(R.drawable.muz)
        }
        nesneResim.degText.text = deger.toString()
        birinciSayi = deger
        islem = 1.toShort()
        ikinciSayi = 0.toShort()
        this.degisken = degisken
    }
    fun degIslemDegDuzenle( birinciDeger: Short, islem: Short, ikinciDeger: Any, degisken: Short){
        var iki: Short = ikinciDeger as Short
        if (degisken == 0.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.elma)

        }
        if (degisken == 1.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.armut)
        }
        if (degisken == 2.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.portakal)
        }
        if (degisken == 3.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.seftali)
        }
        if (degisken == 4.toShort()) {
            nesneResim.anaDegToDeg.setImageResource(R.drawable.muz)
        }
        if (birinciDeger == 0.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.elma)
            birinciSayi = "elma"
        }
        if (birinciDeger == 1.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.armut)
            birinciSayi = "armut"
        }
        if (birinciDeger == 2.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.portakal)
            birinciSayi = "portakal"
        }
        if (birinciDeger == 3.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.seftali)
            birinciSayi = "seftali"
        }
        if (birinciDeger == 4.toShort()) {
            nesneResim.birinciDegiskenDegToDeg.setImageResource(R.drawable.muz)
            birinciSayi = "muz"
        }
        if (islem == 1.toShort()) {
            nesneResim.isaretDegToDeg.setImageResource(R.drawable.arti)
        }
        if (islem == 2.toShort()) {
            nesneResim.isaretDegToDeg.setImageResource(R.drawable.eksi)
        }
        if (islem == 3.toShort()) {
            nesneResim.isaretDegToDeg.setImageResource(R.drawable.carp)
        }
        if (iki == 0.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.elma)
            ikinciSayi = "elma"
        }
        if (iki == 1.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.armut)
            ikinciSayi = "armut"
        }
        if (iki == 2.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.portakal)
            ikinciSayi = "portakal"
        }
        if (iki == 3.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.seftali)
            ikinciSayi = "seftali"

        }
        if (iki == 4.toShort()) {
            nesneResim.ikinciDegiskenDegToDeg.setImageResource(R.drawable.muz)
            ikinciSayi = "muz"

        }
        this.degisken = degisken
        this.islem = islem
    }
    fun degIslemSayiDuzenle(birinciDeger: Short, ikinciDeger: Short, islem: Short, degisken: Short){
        if (islem == 1.toShort())
            nesneResim.isaretIslemToSayi.setImageResource(R.drawable.arti)
        if (islem == 2.toShort())
            nesneResim.isaretIslemToSayi.setImageResource(R.drawable.eksi)
        if (islem == 3.toShort())
            nesneResim.isaretIslemToSayi.setImageResource(R.drawable.carp)
        if (degisken == 0.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.elma)
        } else if (degisken == 1.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.armut)
        } else if (degisken == 2.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.portakal)
        } else if (degisken == 3.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.seftali)
        } else if (degisken == 4.toShort()) {
            nesneResim.anaDegiskenIslemToSayi.setImageResource(R.drawable.muz)
        }
        if (birinciDeger == 0.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.elma)
            birinciSayi = "elma"
        } else if (birinciDeger == 1.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.armut)
            birinciSayi = "armut"
        } else if (birinciDeger == 2.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.portakal)
            birinciSayi = "portakal"
        } else if (birinciDeger == 3.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.seftali)
            birinciSayi = "seftali"
        } else if (birinciDeger == 4.toShort()) {
            nesneResim.birinciDegiskenIslemToSayi.setImageResource(R.drawable.muz)
            birinciSayi = "muz"
        }
        nesneResim.degTextIslemToSayi.text = ikinciDeger.toString()
        this.degisken = degisken
        this.islem = islem
        ikinciSayi = ikinciDeger
    }
    fun degIslemSayiToSayi( birinciSayi: Short, ikinciSayi: Short, islem: Short, degisken: Short){
        if (islem == 1.toShort())
            nesneResim.isaretsayitosayi.setImageResource(R.drawable.arti)
        if (islem == 2.toShort())
            nesneResim.isaretsayitosayi.setImageResource(R.drawable.eksi)
        if (islem == 3.toShort())
            nesneResim.isaretsayitosayi.setImageResource(R.drawable.carp)
        if (degisken == 0.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.elma)
        } else if (degisken == 1.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.armut)
        } else if (degisken == 2.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.portakal)
        } else if (degisken == 3.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.seftali)
        } else if (degisken == 4.toShort()) {
            nesneResim.anadegsayitosayi.setImageResource(R.drawable.muz)
        }


        nesneResim.degTextsayitosayi.text = birinciSayi.toString()
        nesneResim.degText2sayitosayi.text = ikinciSayi.toString()
        this.degisken = degisken
        this.islem = islem
        this.ikinciSayi = ikinciSayi
        this.birinciSayi = birinciSayi
    }
    fun istekDegisimiSart1(){
        if (istek){
            istek=false
            nesneResim.sart1Boolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sart1Boolean.setImageResource(R.drawable.tik)

        }

    }
    fun istekDegisimiSart2(){
        if (istek){
            istek=false
            nesneResim.sart2Boolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sart2Boolean.setImageResource(R.drawable.tik)

        }

    }
    fun istekDegisimiSart3(){
        if (istek){
            istek=false
            nesneResim.sart3Boolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sart3Boolean.setImageResource(R.drawable.tik)

        }

    }
    fun istekDegisimiSartEngel(){
        if (istek){
            istek=false
            nesneResim.sartEngelBoolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sartEngelBoolean.setImageResource(R.drawable.tik)

        }

    }
    fun istekDegisimiSartCizgi(){
        if (istek){
            istek=false
            nesneResim.sartCizgiBoolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sartCizgiBoolean.setImageResource(R.drawable.tik)

        }

    }
    fun cizgiDegisimi(){
        if (kod=="O"){
            kod="S"
            nesneResim.kareCizgi.setImageResource(R.drawable.cizgisag)
        }
        else if(kod=="S"){
            kod="P"
            nesneResim.kareCizgi.setImageResource(R.drawable.cizgisol)

        }
        else if(kod=="P"){
            kod="O"
            nesneResim.kareCizgi.setImageResource(R.drawable.cizgion)

        }

    }
    fun istekDegisimiSartDegToDeg(){
        if (istek){
            istek=false
            nesneResim.sartdegtodegBoolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sartdegtodegBoolean.setImageResource(R.drawable.tik)

        }

    }
    fun istekDegisimiSartDegToSayi(){
        if (istek){
            istek=false
            nesneResim.sartdegtosayiBoolean.setImageResource(R.drawable.carpi)
        }
        else{
            istek=true
            nesneResim.sartdegtosayiBoolean.setImageResource(R.drawable.tik)

        }

    }
    fun renkDegisimi(){
        if (kod=="y"){
            kod="k"
            nesneResim.renkDegisKare.setBackgroundColor(Color.parseColor("#DF0041"))

        }
        else if (kod=="k"){
            kod="m"
            nesneResim.renkDegisKare.setBackgroundColor(Color.parseColor("#3F51B5"))

        }
        else if (kod=="m"){
            kod="z"
            nesneResim.renkDegisKare.setBackgroundColor(Color.parseColor("#00000000"))

        }
        else if (kod=="z"){
            kod="w"
            nesneResim.renkDegisKare.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }
        else if (kod=="w"){
            kod="y"
            nesneResim.renkDegisKare.setBackgroundColor(Color.parseColor("#4DD14A"))
        }
    }
}