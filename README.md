# Sistema Acadêmico - FCTE

## Descrição do Projeto

Desenvolvimento de um sistema acadêmico para gerenciar alunos, disciplinas, professores, turmas, avaliações e frequência, utilizando os conceitos de orientação a objetos (herança, polimorfismo e encapsulamento) e persistência de dados em arquivos.

O enunciado do trabalho pode ser encontrado aqui:
- [Trabalho 1 - Sistema Acadêmico](https://github.com/lboaventura25/OO-T06_2025.1_UnB_FCTE/blob/main/trabalhos/ep1/README.md)

## Dados do Aluno

- **Nome completo:** Júlia Pêgo de Meneses
- **Matrícula:** 231037745
- **Curso:** Engenharia de Software
- **Turma:** T06

---

## Instruções para Compilação e Execução

1. **Compilação:**  
   ```bash
   javac SistemaAlunos.java  
   java SistemaAlunos

2. **Execução:**  

   - **Inicialização**  
     O método `main` em `SistemaAlunos.java` é chamado pela Máquina Virtual Java (JVM).  

   - **Carregamento dos Dados**  
     Um novo objeto `SistemaAlunos` é criado. O método `carregarDados()` carrega dados existentes dos arquivos CSV (se houver) para:  
     - `List<Aluno> alunos`  
     - `List<Disciplina> disciplinas`  
     - `List<Professor> professores`  

   - **Menu Principal**  
     O método `exibirMenu()` exibe o menu interativo:  
     ```
     === SISTEMA ACADÊMICO ===
     1. Gerenciar Alunos
     2. Gerenciar Disciplinas
     ... (etc.)
     ```  
     O programa aguarda a entrada do usuário (opções de 1 a 6).  

   - **Interação com o Usuário**  
     Dependendo da opção escolhida, os métodos como `menuAlunos()`, `menuDisciplinas()`, etc., são chamados.  
     Exemplo: Ao selecionar "1. Gerenciar Alunos", ocorre:  
     - Abertura de um sub-menu para cadastrar, editar ou listar alunos  
     - São chamados métodos como `cadastrarAluno()` ou `listarAlunos()`  

   - **Persistência dos Dados**  
     Ao escolher a opção "6. Salvar e Sair", o método `salvarDados()`:  
     - Grava todas as alterações de volta nos arquivos CSV (`alunos.csv`, `disciplinas.csv`, etc.)  
     - Garante que os dados sejam preservados para a próxima execução  

3. **Estrutura de Pastas:**  

   - **Pasta principal "EP1"**  
     É o diretório principal do projeto, contendo todas as subpastas.  

   - **Subpasta "data"**  
     Armazena todos os arquivos de dados (CSVs).  

   - **Subpasta "src"**  
     Contém todos os arquivos de código-fonte (.java).  

   - **Subpasta "bin"**  
     Armazena arquivos .class compilados.
     
```
EP1/
├── src/
│   ├── SistemaAlunos.java
│   ├── Aluno.java
│   ├── AlunoCsvFormatter.java
│   ├── AlunoEspecial.java
│   ├── CsvService.java
│   ├── Disciplina.java
│   ├── DisciplinaCsvFormatter.java
│   ├── Professor.java
│   ├── ProfessorCsvFormatter.java
│   ├── SistemaAlunos.java
│   └── Turma.java
├── bin/
│   ├── SistemaAlunos.class
│   ├── Aluno.class
│   ├── AlunoCsvFormatter.class
│   ├── AlunoEspecial.class
│   ├── CsvService.class
│   ├── Disciplina.class
│   ├── DisciplinaCsvFormatter.class
│   ├── Professor.class
│   ├── ProfessorCsvFormatter.class
│   └── Turma.class           
└── data/
    ├── alunos.csv
    └── disciplinas.csv
    └── professores.csv
    └── turmas.csv
```

3. **Versão do JAVA utilizada:**  
   Foi utilizado a versão Java 24 (JDK 24).

---

## Vídeo de Demonstração

- https://drive.google.com/file/d/1N6N-mqirj0biYKKDwiSlhvipr48ly9mP/view?usp=sharing

---

## Prints da Execução

1. Menu Principal:  
   ![MenuPrincipal](https://github.com/user-attachments/assets/5e8321d7-3faa-4e24-be74-439804aa1d9c)
)

2. Cadastro de Aluno:  
   ![CadastroAluno](https://github.com/user-attachments/assets/ab04e400-7e89-499a-bf5a-d90290270411)

3. Relatório de Frequência/Notas:  

   ![Inserir Print 3](caminho/do/print3.png)

---

## Principais Funcionalidades Implementadas

- [✓] Cadastro, listagem, matrícula e trancamento de alunos (Normais e Especiais)
- [✓] Cadastro de disciplinas e criação de turmas (presenciais e remotas)
- [✓] Matrícula de alunos em turmas, respeitando vagas e pré-requisitos
- [✓] Lançamento de notas e controle de presença
- [✓] Cálculo de média final e verificação de aprovação/reprovação
- [✓] Relatórios de desempenho acadêmico por aluno, turma e disciplina
- [✓] Persistência de dados em arquivos (.txt ou .csv)
- [✓] Tratamento de duplicidade de matrículas
- [✓] Uso de herança, polimorfismo e encapsulamento

---

## Observações (Extras ou Dificuldades)

- [Espaço para o aluno comentar qualquer funcionalidade extra que implementou, dificuldades enfrentadas, ou considerações importantes.]

---

## Contato

- juliamenes1403@gmail.com
