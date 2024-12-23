Simplificar:
- [ ] Remover permissionLevel
- [ ] Todos podem dar register
- [ ] Add demultiplexer and framedconnections
CommandManager - concurrency on the client
data and accounts managers - concurrency on the server

whats left:
- [x] getWhen (use condition?)

- [x] Limite de utilizadores concorrentes
• Sendo S um parâmetro de configuração, só podem existir no máximo S sessões concorrentes
(diferentes clientes a usar o servidor). Quando atingido, a autenticação de um cliente ficará
em espera até sair outro cliente.

- [ ] fix?: when receiving a notification the comamand gets cut