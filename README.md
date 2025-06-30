# Bear Mod para NeoForge 1.21.1

## Descrição
Este mod foi criado extraindo apenas a entidade "urso" do mod Naturalist original (versão 5.0pre3+forge-1.20.1) e migrando-a para NeoForge 1.21.1.

## Conteúdo Migrado
- **Entidade Bear**: Classe principal do urso com todos os comportamentos
- **Modelo 3D**: Arquivo `bear.geo.json` com o modelo 3D do urso
- **Texturas**: 6 texturas diferentes do urso:
  - `bear.png` - Textura padrão
  - `bear_angry.png` - Urso agressivo
  - `bear_berries.png` - Urso com frutas vermelhas
  - `bear_honey.png` - Urso com mel
  - `bear_sheared.png` - Urso tosquiado
  - `bear_sleep.png` - Urso dormindo
- **Animações**: Arquivo `bear.animation.json` com todas as animações
- **IA e Comportamentos**: Goals de IA incluindo sono, busca por comida, ataque, etc.

## Estrutura do Projeto
```
bear_mod_neoforge/
├── src/main/java/com/starfish_studios/bear_mod/
│   ├── BearMod.java (classe principal)
│   ├── client/
│   │   ├── BearModClient.java
│   │   ├── model/BearModel.java
│   │   └── renderer/BearRenderer.java
│   ├── common/entity/
│   │   ├── Bear.java (entidade principal)
│   │   └── core/ (classes auxiliares)
│   └── registry/ (registros de entidades, sons, tags)
├── src/main/resources/
│   ├── assets/bear_mod/
│   │   ├── textures/entity/bear/ (texturas)
│   │   ├── geo/entity/bear/ (modelo 3D)
│   │   └── animations/entity/bear/ (animações)
│   ├── data/bear_mod/ (loot tables, tags)
│   └── META-INF/neoforge.mods.toml
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Funcionalidades do Urso
- **Comportamentos**: Dormir à noite, procurar comida, atacar quando ameaçado
- **Interações**: Pode ser alimentado com frutas vermelhas, mel e favos de mel
- **Estados visuais**: Diferentes texturas baseadas no estado (dormindo, agressivo, etc.)
- **Animações**: Idle, caminhada, ataque, sono, comer, ficar em pé
- **IA**: Goals personalizados para comportamento realista

## Dependências
- NeoForge 21.1.42+
- Minecraft 1.21.1
- GeckoLib 4.5.8+ (para animações)
- Java 21

## Como Compilar
1. Certifique-se de ter Java 21 instalado
2. Execute: `./gradlew build`
3. O arquivo JAR será gerado em `build/libs/`

## Como Usar
1. Instale NeoForge 1.21.1
2. Coloque o arquivo JAR na pasta `mods`
3. O urso aparecerá naturalmente em biomas apropriados

## Notas Técnicas
- Migrado de Forge 1.20.1 para NeoForge 1.21.1
- Usa GeckoLib para animações avançadas
- Código adaptado para as novas APIs do NeoForge
- Mantém compatibilidade com o comportamento original

## Créditos
- Mod original: Starfish Studios (Naturalist)
- Migração: Baseada no plano de migração fornecido
- Apenas a entidade "urso" foi extraída conforme solicitado

