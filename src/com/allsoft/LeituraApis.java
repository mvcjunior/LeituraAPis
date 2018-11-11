package com.allsoft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeituraApis {

	private final static String REST = "rest";
	private final static String FROM = "from";
	private final static String ON_EXCEPTION = "onException";
	private final static String TO = "to";
	private final static String BEAN = "bean";
	private final static String PROCESS = "process";
	private final static String ERROR_HANDLER = "errorHandler";

	private final static String PUT = "put";
	private final static String POST = "post";
	private final static String PATCH = "patch";
	private final static String DELETE = "delete";
	private final static String GET = "get";

	private final static String PONTO_VIRGULA = ";";
	private final static String SRC = "src";
	private final static String VAZIO = "";
	private final static String ASPAS_DUPLA = "\"";
	private final static String ABRE_PARENTESES = "(";
	private final static String FECHA_PARENTESES = ")";
	private final static String PONTO = ".";

	
	public static void main(String[] args) throws IOException {
		String diretorio = "C:\\Users\\mvcjunior\\workspace\\smart-backend\\src\\main\\java\\com\\br\\all\\empresa_api\\";
		if (args != null && args.length > 0) {
			diretorio = args[0];
		}
		System.out.println("diretorio inicial = " + diretorio);
		List<ClasseRotas> classesRotas = percorreDiretorio(diretorio);
		for (int index1=0; index1 < classesRotas.size(); index1++) {
			if (classesRotas.get(index1).getRotas().size() > 0) {
				for (int index2=0; index2 < classesRotas.get(index1).getRotas().size() ; index2++) {
					DetalheRota detalheRota = detalhamentoRota(classesRotas.get(index1).getRotas().get(index2));
					
					detalheRota.setClasse(nomeClasse(classesRotas.get(index1).getClasse()));
					System.out.println(detalheRota.toString());
				}
			}
		}			
	}
	
	private static String nomeClasse(String classe) {
		int beginIndex = classe.indexOf(SRC) == -1 ? 0 : classe.indexOf(SRC);
		int endIndex = classe.length();
		
		return classe.substring(beginIndex, endIndex);
	}

	private static DetalheRota detalhamentoRota(String rota) {
		List<String> partes = split(rota);
		DetalheRota detalheRota = new DetalheRota();
		List<String> destino = new ArrayList<>();
		List<String> bean = new ArrayList<>();
		List<String> process = new ArrayList<>();

		for (int i=0; i < partes.size(); i++) {
			if (partes.get(i).startsWith(FROM)) {
				detalheRota.setTipo("FROM");
				detalheRota.setOrigem(extraiConteudo(partes.get(i)));
			}
			if (partes.get(i).startsWith(ON_EXCEPTION)) {
				detalheRota.setTipo("ON EXCEPTION");
				detalheRota.setOrigem(partes.get(i));
			}
			if (partes.get(i).startsWith(REST)) {
				detalheRota.setTipo("REST");
				detalheRota.setOrigem(extraiConteudo(partes.get(i)));
			}
			if (partes.get(i).startsWith(ERROR_HANDLER)) {
				detalheRota.setTipo("ERROR HANDLER");
				detalheRota.setOrigem(partes.get(i));
			}
			if (eMetodo(partes.get(i))) {
				detalheRota.setOrigem(detalheRota.getOrigem().concat(extraiConteudo(partes.get(i))));
				detalheRota.setMetodo(extraiMetodo(partes.get(i)));
			}
			if (partes.get(i).startsWith(TO)) {
				destino.add(extraiConteudo(partes.get(i)));
			}
			if (partes.get(i).startsWith(BEAN)) {
				bean.add(extraiConteudoBean(partes.get(i)));
			}
			if (partes.get(i).startsWith(PROCESS)) {
				process.add(extraiConteudoProcess(partes.get(i)));
			}
		}
		detalheRota.setDestino(destino);		
		detalheRota.setBean(bean);		
		detalheRota.setProcess(process);		
		return detalheRota;	
		
	}

	private static List<String> split(String rota) {
		List<String> partes = new ArrayList<>();
		boolean abre = false;
		int inicio = 0;
		for (int i=0; i < rota.length(); i++) {
			if (rota.substring(i, i+1).equals(PONTO) && !abre) {
				partes.add(rota.substring(inicio, i));
				inicio = i + 1;
			}
			if (rota.substring(i, i+1).equals(ABRE_PARENTESES))
				abre = true;
			if (rota.substring(i, i+1).equals(FECHA_PARENTESES))
				abre = false;		
		}
		partes.add(rota.substring(inicio, rota.length()));
		return partes;
	}

	

	private static String extraiConteudoProcess(String string) {
		return string.replaceAll(PONTO_VIRGULA, VAZIO);
	}

	private static String extraiConteudoBean(String string) {

		return string.replaceAll(PONTO_VIRGULA, VAZIO);
	}

	private static String extraiMetodo(String parte) {	
		return parte.substring(0, parte.indexOf(ABRE_PARENTESES)).toUpperCase();
	}

	private static String extraiConteudo(String literal) {
		int inicio = literal.indexOf(ABRE_PARENTESES)+1;
		int fim = literal.indexOf(FECHA_PARENTESES)+1;
		if (literal.contains(ASPAS_DUPLA)) {
			inicio++;
			fim--;
		} 
		
		return literal.substring(inicio, fim-1);
	}

	private static boolean eMetodo(String parte) {
		if (parte.startsWith(GET) || parte.startsWith(POST) || parte.startsWith(PATCH) || parte.startsWith(DELETE) ||
				parte.startsWith(PUT))
			return true;
			
		return false;
	}

	public static List<ClasseRotas> percorreDiretorio(String diretorio) throws IOException {
		List<ClasseRotas> classesRotas = new ArrayList<>();
		File dir = new File(diretorio);
		System.out.println(" verificando " + diretorio);
		File[] dir1 = dir.listFiles();
		for (int i=0; i < dir1.length;i++) {
			if ( dir1[i].isDirectory()) {
				classesRotas.addAll(percorreDiretorio(dir1[i].getPath()));
			} else {
				if ( dir1[i].isFile()) {
					classesRotas.add(avaliaCodigoJava(dir1[i].getPath()));
				}
			}
		}
		
		return classesRotas;
	}
	
	public static ClasseRotas avaliaCodigoJava(String arquivo) throws IOException {
		ClasseRotas classeRotas = new ClasseRotas();
		List<String> rotas = new ArrayList<>();
		
		BufferedReader br = new BufferedReader(new FileReader(arquivo));
		
		boolean component = false;
		boolean configure = false;

		boolean rota = false;
		StringBuffer temp = new StringBuffer();
		while(br.ready()){
		   String linha = br.readLine();
		   if (component && configure) {
			   if (linha.contains(FROM) || linha.contains(REST)) {
				   rota = true;
			   }
				   
			   if (linha.contains(PONTO_VIRGULA)) {
				   temp.append(linha.trim());
				   rotas.add(temp.toString());
				   temp.delete(0, temp.length());
				   rota = false;
			   }
			   
			   if (rota) {
				   temp.append(linha.trim());
			   }
		   }
		   if (linha.contains("@Component"))
			   component = true;
		   if (linha.contains("configure()"))
			   configure = true;
		}
		br.close();
		classeRotas.setClasse(arquivo);
		classeRotas.setRotas(rotas);
		return classeRotas;
	}

}

class DetalheRota {
	
	private final static String VAZIO = "";
	private final static String BRANCO = " ";
	private final static String ASPAS_DUPLA = "\"";
	private final static String VIRGULA_BRANCO = ", ";
	private final static String DOIS_PONTOS = ": ";

	private String tipo;
	private String origem;
	private String metodo;
	private List<String> process;
	private List<String> bean;
	private List<String> destino;
	private String classe;

	public String getTipo() {
		return tipo;
	}
	public String getOrigem() {
		return origem;
	}
	public String getMetodo() {
		return metodo;
	}
	public List<String> getDestino() {
		return destino;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public void setOrigem(String origem) {
		this.origem = origem;
	}
	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}
	public void setDestino(List<String> destino) {
		this.destino = destino;
	}
	
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	
	public List<String> getProcess() {
		return process;
	}
	public List<String> getBean() {
		return bean;
	}
	public void setProcess(List<String> process) {
		this.process = process;
	}
	public void setBean(List<String> bean) {
		this.bean = bean;
	}
	@Override
	public String toString() {
		StringBuffer conteudo = new StringBuffer();

		conteudo.append("{ ");
		conteudo.append(tipo != null ? formata(tipo, "tipo") : VAZIO);
		conteudo.append(origem != null ? VIRGULA_BRANCO + formata(origem, "origem") : VAZIO);
		conteudo.append(metodo != null ? VIRGULA_BRANCO + formata(metodo, "metodo") : VAZIO);
		conteudo.append(classe != null ? VIRGULA_BRANCO + formata(classe, "classe") : VAZIO);
		conteudo.append(destino != null && destino.size() > 0 ? VIRGULA_BRANCO + formataLista(destino, "destino") : VAZIO);
		conteudo.append(bean != null && bean.size() > 0 ? VIRGULA_BRANCO + formataLista(bean, "bean") : VAZIO);
		conteudo.append(process != null && process.size() > 0 ? VIRGULA_BRANCO + formataLista(process, "process") : VAZIO);	
		conteudo.append(" }");
		
		return conteudo.toString();
	}
	
	private String formataLista(List<String> lista, String literal) {
		StringBuffer retorno = new StringBuffer(VAZIO);

		retorno.append(ASPAS_DUPLA); 
		retorno.append(literal);
		retorno.append(ASPAS_DUPLA);
		retorno.append(BRANCO);
		retorno.append(DOIS_PONTOS);

		for (int i=0; i < lista.size()-1; i++) {
			retorno.append(ASPAS_DUPLA);
			retorno.append(lista.get(i));
			retorno.append(ASPAS_DUPLA);
			retorno.append(VIRGULA_BRANCO);

		}
		retorno.append(ASPAS_DUPLA);
		retorno.append(lista.get(lista.size()-1));
		retorno.append(ASPAS_DUPLA);
		retorno.append("}");
		return retorno.toString();
	}
	
	private String formata(String campo, String literal) {
		StringBuffer retorno = new StringBuffer(VAZIO);
		retorno.append(ASPAS_DUPLA);
		retorno.append(literal);
		retorno.append(ASPAS_DUPLA);
		retorno.append(BRANCO);
		retorno.append(DOIS_PONTOS);
		retorno.append(ASPAS_DUPLA);
		
		retorno.append(campo);
		retorno.append(ASPAS_DUPLA);
		return retorno.toString();
	}
	
}

class ClasseRotas {
	private String classe;
	private List<String> rotas;
	public String getClasse() {
		return classe;
	}
	public List<String> getRotas() {
		return rotas;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	public void setRotas(List<String> rotas) {
		this.rotas = rotas;
	}
	
}
