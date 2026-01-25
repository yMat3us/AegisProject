# Aegis - O Escudo Inteligente para Servidores Discord

**Aegis** é um bot inteligente, modular e seguro, desenvolvido em **Java** com a biblioteca **JDA (Java Discord API)**. Inspirado no conceito de um escudo absoluto, Aegis atua como um verdadeiro guardião digital para servidores Discord, proporcionando automação avançada, moderação eficiente e integração extensível com APIs externas.

## Recursos Planejados
⚠️ **O projeto está atualmente em desenvolvimento**. Recursos ainda serão adicionados. Aqui está uma visão geral do que está planejado:

- **Integração com banco de dados MongoDB**: Armazenamento seguro e persistente para diversas informações do bot e do servidor.
- **Comandos de moderação**: Sempre protegendo o servidor contra comportamento inadequado ou usuários indesejáveis.
- **Segurança reforçada**: Sistema de verificação e ações automáticas contra comportamentos suspeitos.
- **Minigames interativos**: Jogos simples, engajadores e integrados ao Discord, para entretenimento da comunidade.
- **Painel de configuração**: Plano para desenvolvimento de uma interface web com TypeScript que permite gerenciar o bot diretamente.

## Funcionalidades atuais

### 🎵 **Música**
O **Aegis** já conta com recursos avançados de música:
- Pesquisa e reprodução de músicas utilizando **Lavalink** para qualidade de som superior.
- Seleção interativa de música com botões e menus no Discord.
- Suporte para várias origens, como YouTube e Spotify.

### ⚙️ **Extensibilidade**
A estrutura é modular, permitindo fácil adição de novos comandos e serviços.

### 🛡️ **Segurança**
Gerenciamento de permissões e controle granulado para proteger canais, membros e conteúdos.

## Configuração do Bot
Para configurar e executar o Aegis, siga as instruções abaixo:

### Pré-requisitos
- **Java JDK 11 ou superior**
- **MongoDB Comunitário** (se já integrado no momento da execução)
- Token do Bot do Discord
- Configuração do Lavalink (host, porta e senha)

### Passos
1. Clone este repositório:
   ```bash
   git clone https://github.com/yMat3us/AegisProject.git
   ```
2. Atualize os detalhes de configuração no arquivo `BotConfig.java`:
   ```java
   public static final String TOKEN = "Seu-Token-Discord";
   public static final String lavalinkPassword = "Sua-Senha";
   public static final String lavalinkName = "Nome-Interno";
   public static final String lavalinkHost = "Host-do-Lavalink";
   public static final int lavalinkPort = 2333; // Porta Padrão
   ```

3. Compile o projeto com **Maven**:
   ```bash
   mvn package
   ```
4. Execute o bot:
   ```bash
   java -jar target/Aegis.jar
   ```

## Planejamento do Site
Uma interface de configurações será incluída futuramente:
- **Ferramentas de moderação**: Configuração de banimentos automáticos, auditoria e permissões.
- **Painel web em TypeScript**: Integrando com o bot para maior usabilidade (gerenciamento de streaming, logs e atividades).

## Contribuindo
Contribuições são bem-vindas! Antes de começar:
1. Abra uma issue para discutir grandes mudanças.
2. Certifique-se de seguir o estilo do código existente.

## Licença
Este projeto está licenciado sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

💡 “Aegis protege, organiza e potencializa sua comunidade com inteligência e segurança.”
