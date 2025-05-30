# ReScanner – Aplicatie mobila pentru scanarea si analiza cecurilor



**ReScanner** este o aplicatie Android inteligenta, care automatizeaza procesul de capturare, procesare si analiza a bonurilor fiscale. Proiectata initial ca un proiect academic, aplicatia a evoluat intr-un instrument util pentru gestiunea financiara personala si automatizarea evidentei cheltuielilor.

## 🚀 Ce face aplicatia

* Captureaza imaginea unui cec folosind camera telefonului sau prin incarcarea din galerie
* Foloseste OCR si modele AI (Donut + DeepSeek API) pentru a extrage date relevante:

  * Nume magazin
  * Data achizitiei
  * Lista de produse si preturile
  * Reduceri
  * Total suma
* Afiseaza rezultatele intr-un UI intuitiv
* Permite trimiterea informatiilor scanate prin email sau notificari catre contacte salvate
* Ofera o componenta de gamificare care motiveaza utilizatorul sa-si tina cheltuielile sub control

## 🤖 Functionalitati cheie

| Functionalitate        | Descriere                                                        |
| ---------------------- | ---------------------------------------------------------------- |
| Scanare bonuri         | Cu ajutorul camerei sau din galerie                              |
| OCR on-device          | Cu ML Kit (Google)                                               |
| AI pentru analiza      | DeepSeek API pentru interpretarea textului        |
| Clasificare cheltuieli | Automata sau manuala                                             |
| Rapoarte lunare        | Pe categorii, cu grafice                                         |
| Notificari             | SMS, email sau push (in functie de preferintele userului)        |
| Modul joc              | Utilizatorul primeste insigne si obiective atinse (gamification) |


1. Scanarea facuta pe un cec real
   
![image](https://github.com/user-attachments/assets/51072992-1430-49fe-a6d0-8e3ccaa284af)

2. Suma totala a cecurilor + insigne castigate
   
![image](https://github.com/user-attachments/assets/77802f21-5d9f-4352-a9ba-e38036b24531)

3. Cheluielile pe luna
   
![image](https://github.com/user-attachments/assets/b3060390-a221-42a2-94e5-ea853c50252a)
![image](https://github.com/user-attachments/assets/c3efa726-3b13-41e0-b9bd-6e03bd161541)

4. Lista de contacte si adaugarea lor
   
![image](https://github.com/user-attachments/assets/c77a7f5c-ad71-4b60-8515-30cfd2465605)
![image](https://github.com/user-attachments/assets/7670e194-d82d-4c76-b1a8-f5e4d717a4f6)

5. Notificarile primite in aplicatie
   
![image](https://github.com/user-attachments/assets/300448f3-5b23-484a-ab0c-6fa188aa0d94)

6. Setarile generale de aplicatie
   
![image](https://github.com/user-attachments/assets/afc070a2-8fd7-4cd6-9794-d23160b25034)

7. Bara de navigare a aplicatiei
   
![image](https://github.com/user-attachments/assets/351932ad-b4e7-4245-81c7-6addc2efb494)

## 🚚 Arhitectura aplicatiei

* Kotlin + Android SDK
* ML Kit pentru OCR
* TensorFlow Lite pentru rulare model AI pe device
* DeepSeek API pentru analiza semantica a textului
* Room pentru stocare locala a datelor
* Retrofit + GSON pentru integrarea cu servicii externe (DeepSeek)

### Structura codului:

* O cauti si singurel in repository))

## ⚖️ Tehnologii si instrumente

* **CameraX**: pentru capturarea imaginii
* **ML Kit OCR**: pentru recunoasterea textului din bonuri
* **TFLite**: model AI local pentru extragere structura date
* **DeepSeek API**: analiza contextuala a textului
* **Room DB**: salvare bonuri si date local
* **Android Jetpack**: lifecycle, navigation, viewmodel
* **Retrofit & GSON**: comunicare REST

## 🔧 Cerinte minime

* Android 8.0+
* Camera functionala cu autofocus
* Acces la stocare (pentru salvare bonuri)
* Conexiune internet (doar pentru AI avansat prin API)

## 🔎 Planuri viitoare

* Suport pentru recunoastere multi-limba (RO/EN/DE)
* Sincronizare cloud optionala (conturi multiple)
* Export CSV/PDF al rapoartelor
* Modul business (pentru firme mici care isi tin cheltuielile)

## 🎓 Despre proiect

Aceasta aplicatie a fost dezvoltata in cadrul cursului "Tehnologii si mecanisme de proiectare a produselor software" de catre studentii Frimu Dumitru si Guțu Nicoleta, sub indrumarea asist. univ. Scrob Sergiu (UTM, 2025).

## ✉️ Contact

Pentru intrebari, feedback sau colaborari:

* dumitru.frimu\[at]student.utm.md
* nicoleta.gutu\[at]student.utm.md

---

**DISCLAIMER**: Aplicatia este inca in dezvoltare si nu garanteaza 100% acuratete la scanare. Testata pe bonuri din MD si RO.

---

> "Scan smarter. Save faster. CheckScan."
