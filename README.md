# GADM Mobile v3.0.0 — Núcleo Operacional Estável

Este pacote é uma reconstrução limpa do GADM Mobile. Ele não reaproveita a tela quebrada que apresentava `rememberSaveable` ausente, chamadas `suspend` fora de coroutine e erros de escopo Compose.

## O que está realmente entregue

- Login por **Firebase Authentication** (e-mail/senha).
- Controle de acesso por documento `usuarios/{uid}` com `role: ADMIN` ou `GESTOR`.
- Leitura em tempo real da coleção Firestore `pedidos`.
- Painéis de Visão geral, Pedidos, Cozinha, Torre de despacho e Ajustes.
- Pedido detalhado: cliente, endereço, itens, pagamento, taxa, observação e entregador.
- Fluxo rígido de status:

```
RECEBIDO → CONFIRMADO → EM_PREPARO → PRONTO → AGUARDANDO_ENTREGADOR → EM_ROTA → ENTREGUE
RECEBIDO ou CONFIRMADO → CANCELADO
```

- O app bloqueia transições fora da sequência.
- Cada mudança grava um item em `timeline`, mais campos operacionais (`preparoIniciadoEm`, `prontoEm`, `saiuParaEntregaEm`, `entregueEm`, etc.).
- Atribuição de entregador somente para pedido pronto/na torre.
- Prioridade operacional, sem pedido fictício e sem conta simulada.
- Workflow GitHub Actions que gera o APK debug.

## O que precisa ser conectado uma única vez

1. Copie o `google-services.json` **real** do seu Firebase para:

```text
app/google-services.json
```

2. No Firebase Authentication, crie o e-mail/senha do administrador ou gestor.

3. No Firestore, crie o documento abaixo usando o **UID** desse usuário:

```text
coleção: usuarios
documento: UID_DO_USUARIO
campos:
  nome: "Diego"
  role: "ADMIN"
```

Aceitos: `ADMIN` e `GESTOR`.

4. Publique as regras de `firestore.rules` no Firebase Console **após revisar a estrutura do seu app Cliente**. As regras são uma base segura e devem ser ajustadas se seu Cliente criar pedidos sem login.

## Estrutura mínima esperada de um pedido

O leitor é tolerante com campos legados, mas o padrão novo recomendado é:

```json
{
  "numero": "3041",
  "status": "RECEBIDO",
  "cliente": { "nome": "Nome", "telefone": "67999999999", "uid": "uid-cliente" },
  "endereco": { "logradouro": "Rua A", "numero": "10", "bairro": "Centro", "referencia": "Portão preto" },
  "itens": [
    { "quantidade": 1, "nome": "Açaí 700 ml", "detalhes": "Origem • leite em pó" }
  ],
  "total": 27.0,
  "taxaEntrega": 3.0,
  "pagamento": { "metodo": "PIX", "trocoPara": "" },
  "observacoes": "Sem colher",
  "prioridade": false,
  "createdAt": "serverTimestamp()"
}
```

## Gerar APK no GitHub

No repositório `up-gadm-mobile`:

```bash
cd ~
rm -rf up-gadm-mobile
unzip -o /sdcard/Download/GADM-Mobile-v3.0.0-Estavel.zip -d ~
mv ~/GADM-Mobile-v3.0.0-Estavel ~/up-gadm-mobile
cd ~/up-gadm-mobile

# Antes de enviar, coloque o google-services.json real em app/.
git add .
git commit -m "GADM Mobile v3.0.0 - núcleo operacional estável"
git push origin main
```

Depois, no GitHub: **Actions → Gerar APK GADM Mobile → Run workflow**.

O artefato terá o nome `GADM-Mobile-v3.0.0-debug-apk`.

## Gerar no Termux

```bash
cd ~/up-gadm-mobile
chmod +x build_termux.sh
./build_termux.sh
```

## Observações importantes

- O APK não inclui o `google-services.json` verdadeiro porque isso contém informações do seu projeto Firebase.
- Não use senha/PIN gravado dentro do aplicativo como segurança operacional. O acesso real desta versão é Firebase Auth + perfil no Firestore.
- Esta versão entrega o núcleo GADM Mobile. Ela não substitui ainda o painel PC completo, a geolocalização avançada, chat de atendimento, impressão ou motor automático de rotas.
