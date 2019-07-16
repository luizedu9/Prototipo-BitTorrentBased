# Prototipo-BitTorrentBased

Instituto Federal de Educação, Ciência e Tecnologia de Minas Gerais, IFMG - Campus Formiga 

Ciência da Computação

Protótipo de transferencia de arquivos baseada em BitTorrent.

Autores: Álissom Vieira da Cunha, Luiz Eduardo Pereira, Kimberly Lamounier Campos Ferreira.

# Proposta:

Deverá ser projetado um programa para disseminação eficiente de arquivo, inspirado no protocolo BitTorrent, com a comunicação entre os processos realizada através da API sockets (fluxo de bytes TCP e/ou datagramas UDP). O programa deverá utilizar arquitetura par-a-par e apresentar uma escalabilidade melhor do que protocolos de transferência de arquivos que utilizem arquitetura cliente/servidor (ex.: FTP, HTTP).

Mais informações em Proposta.pdf e Relatorio.pdf.

# Objetivos Alcançados:

Formato de arquivo de metadados: O metadado armazena o tamanho do arquivo original, o tamanho e a quantidade das peças e o tamanho da última peça, que recebe um tratamento especial, podendo ser menor que as outras do arquivo original. As informações de cada peça são criptografadas em uma hash SHA-1.

Mecanismo de transferência: O objetivo do trabalho é a criação de uma transferência par-a-par, porém não foi alcançado. Foi implementado mas não em seu devido funcionamento, logo foi escolhido a transferência da forma Cliente-Servidor, que está realizando a transferência com êxito, sendo implementado em API Sockets TCP.

Mecanismo de verificação: Foi utilizado o SHA-1. Quando a peça chega para o cliente, o seu verificador confere a integridade da peça e, se compatível com a hash referente a peça no metadado, confirma sua totalidade.

Interface com o usuário: Utilizando o CLI, com os comandos: “montar” para criar o arquivo metadado; “seeder” para enviar o arquivo; “leecher” para receber o arquivo.

Política de seleção de peças: Utilizado a seleção aleatória.
