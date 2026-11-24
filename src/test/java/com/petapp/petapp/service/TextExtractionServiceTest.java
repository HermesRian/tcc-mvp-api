package com.petapp.petapp.service;

import com.petapp.petapp.entity.PetInfo;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Avaliacao da extracao de texto com um conjunto NOVO de 6 publicacoes,
 * escrito pelo autor, com o gabarito definido ANTES de ajustar as regras.
 * O teste imprime um relatorio campo a campo e falha se houver divergencia.
 *
 * Regras de comparacao:
 * - campo vazio no gabarito: se o sistema extrair algo, conta como inventado;
 * - cor e tamanho: comparacao exata, aceitando variacao de genero (branca/branco);
 * - local: aceito se o extraido contem ou esta contido em alguma das opcoes do gabarito;
 * - contato: compara apenas os digitos.
 */
class TextExtractionServiceTest {

    private record Caso(String id, String texto, String tipo, String status, String local,
                        String data, String periodo, String cor, String tamanho, String contato) {}

    private static final List<Caso> CASOS = List.of(
        new Caso("N1",
            "gatinho perdido na região do morumbi. sumiu ontem pela tarde. cor preto e branco. whatsapp: 45998412548;",
            "Gato", "Desaparecido", "morumbi", "", "tarde", "preto e branco", "", "45998412548"),
        new Caso("N2",
            "cachorro desaparecido. ultima vez visto foi ontem à noite. no bairro do portal da foz. ele é um vira-lata caramelo. meu número é 459985126;",
            "Cachorro", "Desaparecido", "portal da foz", "", "noite", "caramelo", "", "459985126"),
        new Caso("N3",
            "Minha gatinha sumiu no dia 08 pela noite aqui na Por do Sol, próximo a Praça da Bíblia. Ela é toda branca e atende pelo nome de Fedora. contato: (45)99801-2368;",
            "Gato", "Desaparecido", "por do sol|praça da bíblia", "", "noite", "branco", "", "45998012368"),
        new Caso("N4",
            "Meu cachorro sumiu na tarde dessa segunda-feira perto do Muffato da República Argentina. É um pastor alemão marrom chamado Theo. qualquer informação, chamar: 45 998041678;",
            "Cachorro", "Desaparecido", "muffato da república argentina", "", "tarde", "marrom", "", "45998041678"),
        new Caso("N5",
            "perdi minha gata hoje aqui no bairro jardim panorama. ela é alaranjada e atende por Alice. telefone 45998036745;",
            "Gato", "Desaparecido", "jardim panorama", "", "", "alaranjado", "", "45998036745"),
        new Caso("N6",
            "URGENTE! cachorro desaparecido no centro de foz, na av Brasil. É um shitzu branco com cinza. sumiu na terça feira no final da tarde. ofereço recompensa, chamar 45998440276.",
            "Cachorro", "Desaparecido", "centro de foz|av brasil", "", "tarde", "branco e cinza", "", "45998440276")
    );

    // Conjunto de VALIDACAO: escrito depois do ajuste das regras e nunca usado para ajusta-las.
    // O resultado deste conjunto e o que vale como medida de generalizacao.
    private static final List<Caso> CASOS_VALIDACAO = List.of(
        new Caso("V1",
            "ACHAMOS uma cachorrinha caramelo, filhote, vagando perto do mercado Gazin na tarde de ontem. Está com a gente aqui em casa. Quem for o dono, ligue 45 99112-3344.",
            "Cachorro", "Encontrado", "gazin", "", "tarde", "caramelo", "filhote", "45991123344"),
        new Caso("V2",
            "Meu cachorro Thor desapareceu, é um labrador preto. Foi dia 12/11 pela manhã no Jardim América. Ele tem coleira vermelha. Cel 45 99887-6655, por favor.",
            "Cachorro", "Desaparecido", "jardim américa", "2026-11-12", "manhã", "preto", "", "45998876655"),
        new Caso("V3",
            "Gatinha siamesa fugiu de casa ontem à noite, na Rua das Palmeiras, perto da escola. Pequena e muito assustada. Contato: 45 99221-0099",
            "Gato", "Desaparecido", "rua das palmeiras", "", "noite", "", "pequeno", "45992210099"),
        new Caso("V4",
            "ENCONTRADO! Cachorro pitbull marrom com focinho branco no Parque Monjardim, Foz do Iguaçu. Parece ter dono pois usa coleira. Contato: 45 99600-1122",
            "Cachorro", "Encontrado", "parque monjardim", "", "", "marrom", "", "45996001122"),
        new Caso("V5",
            "Gente, minha gata Mel está desaparecida desde o dia 05/11. Ela é cinza, castrada e tem uns 3 anos. Última vez vista na Vila Portes. Quem tiver informação, whatsapp 45 99345-7788.",
            "Gato", "Desaparecido", "vila portes", "2026-11-05", "", "cinza", "", "45993457788"),
        new Caso("V6",
            "Perdi meu periquito verde hoje cedo no bairro Porto Belo. Ele responde pelo nome de Zeca. Se alguém viu, me chame no 45 99771-0505.",
            "Pássaro", "Desaparecido", "porto belo", "", "", "verde", "", "45997710505"),
        new Caso("V7",
            "Encontrei um gato laranja na Av. Paraná, em frente ao Posto Ipiranga, domingo de manhã. Está bem e comeu. Mando foto no privado. Telefone: (45) 99650-4321",
            "Gato", "Encontrado", "paraná|posto ipiranga", "", "manhã", "laranja", "", "45996504321"),
        new Caso("V8",
            "cachorra idosa desaparecida, branca e sem uma pata. Sumiu na manhã de 10/11, no Centro, perto do Terminal Urbano. Pelo amor de Deus, ajudem. Fone 45 99010-2030",
            "Cachorro", "Desaparecido", "centro|terminal urbano", "2026-11-10", "manhã", "branco", "idoso", "45990102030")
    );

    // Conjunto ORIGINAL (D1 a X2): os 8 posts do primeiro teste, com o gabarito escrito antes
    // da primeira medicao. Serve como teste de regressao depois do ajuste das regras.
    private static final List<Caso> CASOS_ORIGINAL = List.of(
        new Caso("D1",
            "Gata tricolor desaparecida na manhã do dia 20/10, no bairro Vila Yolanda. Adulta, muito dócil. Contato: (45) 99999-1111",
            "Gato", "Desaparecido", "vila yolanda", "2026-10-20", "manhã", "tricolor", "adulto", "45999991111"),
        new Caso("E1",
            "Encontrei uma gata tricolor hoje à tarde, perto da Avenida Brasil. Parece perdida. Quem for a dona, ligar (45) 99999-2222",
            "Gato", "Encontrado", "avenida brasil", "", "tarde", "tricolor", "", "45999992222"),
        new Caso("D2",
            "URGENTE: gato preto de patas brancas sumido desde ontem, em frente ao Parque Nacional. Idoso e precisa de medicação. Whatsapp: 45 98888-3333",
            "Gato", "Desaparecido", "parque nacional", "", "", "preto", "idoso", "45988883333"),
        new Caso("E2",
            "Gato preto encontrado no bairro Porto Meira, no dia 22/10 de madrugada. Está bem, aguardando o tutor.",
            "Gato", "Encontrado", "porto meira", "2026-10-22", "madrugada", "preto", "", ""),
        new Caso("D3",
            "Cachorra branca de porte médio desaparecida, saiu de casa no centro na tarde de 15/10/2026.",
            "Cachorro", "Desaparecido", "centro", "2026-10-15", "tarde", "branco", "médio", ""),
        new Caso("E3",
            "Encontrada cachorrinha branca, na Rua Rio Branco. Contato 45 97777-4444.",
            "Cachorro", "Encontrado", "rua rio branco", "", "", "branco", "", "45977774444"),
        new Caso("X1",
            "Alguém viu minha tartaruga? Fugiu ontem do quintal.",
            "Outro", "Desaparecido", "", "", "", "", "", ""),
        new Caso("X2",
            "Feliz dia das crianças a todos!",
            "", "", "", "", "", "", "", "")
    );

    @Test
    void avaliaExtracaoNoConjuntoOriginal() {
        avalia("conjunto ORIGINAL (D1 a X2), regressao", CASOS_ORIGINAL);
    }

    @Test
    void avaliaExtracaoNoConjuntoDeAjuste() {
        avalia("conjunto de ajuste (N1 a N6)", CASOS);
    }

    @Test
    void avaliaExtracaoNoConjuntoDeValidacao() {
        avalia("conjunto de VALIDACAO (V1 a V8)", CASOS_VALIDACAO);
    }

    private void avalia(String titulo, List<Caso> casos) {
        TextExtractionService service = new TextExtractionService();
        List<String> falhas = new ArrayList<>();
        int esperados = 0, corretos = 0, naoExtraidos = 0, errados = 0, inventados = 0;

        for (Caso c : casos) {
            PetInfo p = service.extractInformation(c.texto());
            String[][] campos = {
                {"tipo", c.tipo(), p.getPetType()},
                {"status", c.status(), p.getPetStatus()},
                {"local", c.local(), p.getLocation()},
                {"data", c.data(), p.getEventDate() == null ? null : p.getEventDate().toString()},
                {"periodo", c.periodo(), p.getEventTimePeriod()},
                {"cor", c.cor(), p.getColor()},
                {"tamanho", c.tamanho(), p.getSize()},
                {"contato", c.contato(), p.getContactInfo()}
            };
            for (String[] f : campos) {
                String campo = f[0], esperado = f[1], obtido = f[2];
                boolean temEsperado = !esperado.isEmpty();
                boolean temObtido = obtido != null && !obtido.isBlank();
                String resultado;
                if (temEsperado) {
                    esperados++;
                    if (!temObtido) { resultado = "NAO EXTRAIU"; naoExtraidos++; }
                    else if (confere(campo, esperado, obtido)) { resultado = "OK"; corretos++; }
                    else { resultado = "ERRADO"; errados++; }
                } else {
                    if (temObtido) { resultado = "INVENTADO"; inventados++; }
                    else resultado = "-";
                }
                if (!resultado.equals("OK") && !resultado.equals("-")) {
                    falhas.add(String.format("%s | %-8s | esperado='%s' | obtido='%s' | %s",
                        c.id(), campo, esperado, obtido, resultado));
                }
            }
        }

        int extraidos = corretos + errados + inventados;
        System.out.println("===== RELATORIO DE EXTRACAO: " + titulo + " =====");
        falhas.forEach(System.out::println);
        System.out.printf("Esperados=%d Corretos=%d NaoExtraidos=%d Errados=%d Inventados=%d%n",
            esperados, corretos, naoExtraidos, errados, inventados);
        System.out.printf("Precisao=%.0f%% Cobertura=%.0f%%%n",
            extraidos == 0 ? 0 : 100.0 * corretos / extraidos,
            esperados == 0 ? 0 : 100.0 * corretos / esperados);

        assertTrue(falhas.isEmpty(), "Divergencias:\n" + String.join("\n", falhas));
    }

    private static boolean confere(String campo, String esperado, String obtido) {
        String e = norm(esperado), o = norm(obtido);
        switch (campo) {
            case "contato":
                return e.replaceAll("\\D", "").equals(o.replaceAll("\\D", ""));
            case "local":
                for (String alt : e.split("\\|")) {
                    if (o.contains(alt.trim()) || alt.trim().contains(o)) return true;
                }
                return false;
            case "cor":
            case "tamanho":
                return semGenero(e).equals(semGenero(o));
            default:
                return e.equals(o);
        }
    }

    private static String semGenero(String s) {
        return s.replaceAll("([a-z])[ao]\\b", "$1");
    }

    private static String norm(String s) {
        String n = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "");
    }
}
