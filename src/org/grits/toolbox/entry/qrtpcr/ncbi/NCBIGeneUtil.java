package org.grits.toolbox.entry.qrtpcr.ncbi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class NCBIGeneUtil {
	
	private static final Logger logger = Logger.getLogger(NCBIGeneUtil.class);
	
	private String ncbiGeneDbSearchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=";
	private String ncbiGeneDbDetailsUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=gene&retmode=xml&id=";
	private static String symbolSearch = "[sym]";
	private static String organismSearch="[orgn]";
	
	public Integer getGeneIdFromNCBI (String geneSymbol, String organism) throws MalformedURLException, Exception {
        String t_strAnswer = this.makeHttpRequest(new URL(this.ncbiGeneDbSearchUrl + geneSymbol.toString() + symbolSearch + organism + organismSearch));
        Integer geneId = parseSearchResult (t_strAnswer);
        if (geneId == null)
        	logger.info("Could not find. url is " + this.ncbiGeneDbSearchUrl + geneSymbol.toString() + symbolSearch + organism + organismSearch);
        return geneId;
	}
	
	public Integer getGeneIdFromNCBIByRefSeq (String refSeq, String organism) throws MalformedURLException, Exception {
        String t_strAnswer = this.makeHttpRequest(new URL(this.ncbiGeneDbSearchUrl + refSeq.toString() ));
        Integer geneId = parseSearchResult (t_strAnswer);
        if (geneId == null)
        	logger.info("Could not find. url is " + this.ncbiGeneDbSearchUrl + refSeq.toString() );
        return geneId;
	}

    @SuppressWarnings("unchecked")
	private Integer parseSearchResult(String xmlString) throws JDOMException, IOException {
    	Integer geneId = null;
    	SAXBuilder t_builder = new SAXBuilder();
		Document document = t_builder.build(new StringReader(xmlString));
		Element t_elmentRoot = document.getRootElement();
		if ( !t_elmentRoot.getName().equals("eSearchResult") )
		{
			return null;
		}
		List<Element> t_childIdList = t_elmentRoot.getChildren("IdList");
		if ( t_childIdList.size() != 1 )  //TODO: what to do if we get multiple results
		{
			return null;
		}
		
		for (Element t_element : t_childIdList) 
		{
			List<Element> t_childItem = t_element.getChildren("Id");
			if (t_childItem.size() == 0) {
				return null;
			}
			String geneIdString = t_childItem.get(0).getText();
			try {
				geneId = Integer.parseInt(geneIdString);
			} catch (NumberFormatException e) {
				return null;
			}
			break;
		}
		
		return geneId;
	}
    
    public Gene getDetailsFromNCBI (Integer geneId) throws MalformedURLException, Exception {
    	Gene gene = new Gene();
    	gene.addGeneId(geneId);
    	
    	String t_strAnswer = this.makeHttpRequest(new URL(this.ncbiGeneDbDetailsUrl + geneId));
    	if (!parseFullReportResult(t_strAnswer, gene)) {
    		return null;
    	}
    	return gene;
    }

	/**
	 * Parse the xml and fill the Hashmap which contains each item tag with its name attribute as key.
	 * @param a_strXML XML string.
	 * @return True if the format was valid, false otherwise.
	 * @throws JDOMException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private boolean parseFullReportResult(String t_strAnswer, Gene gene) throws JDOMException, IOException {
		List<String> aliasList = new ArrayList<>();
		
		SAXBuilder t_builder = new SAXBuilder();
		Document doc = t_builder.build(new StringReader(t_strAnswer));
		Element t_elmentRoot = doc.getRootElement();
		if ( !t_elmentRoot.getName().equals("Entrezgene-Set") )
		{
			return false;
		}
		List<Element> t_childgene = t_elmentRoot.getChildren("Entrezgene");
		if ( t_childgene.size() != 1 )
		{
			return false;
		}
		
		//Aliases
		XPath xpath = null;
		xpath = XPath.newInstance("//Entrezgene_gene/Gene-ref/Gene-ref_syn");
		List<Element> resultList = xpath.selectNodes( t_childgene.get(0));
		
		for (Element t_element : resultList) 
		{
			List<Element> t_childItem = t_element.getChildren("Gene-ref_syn_E");
			for (Element t_elementItem : t_childItem) 
			{
				String t_strValue = t_elementItem.getText();
				if ( t_strValue != null )
				{
					aliasList.add(t_strValue);
				}
			}			
		}
		
		//Locations
		List<String> locations = new ArrayList<String>();
		List<String> mRNARefSeqList = new ArrayList<>();
		List<String> proteinRefSeqList = new ArrayList<>();
		xpath = XPath.newInstance("//Entrezgene_locus/Gene-commentary");
		resultList = xpath.selectNodes( t_childgene.get(0));
		for (Element t_element : resultList) 
		{
			List<Element> t_childItem = t_element.getChildren("Gene-commentary_accession");
			if (t_childItem.size() > 0) {
				String loc = t_childItem.get(0).getText();
				if (!locations.contains(loc))
					locations.add(t_childItem.get(0).getText());
			}
		}
		
		//RefSeqs
		xpath = XPath.newInstance("//Entrezgene_comments/Gene-commentary");
		resultList = xpath.selectNodes( t_childgene.get(0));
		for (Element result : resultList) {
			Element typeElement1 = result.getChild("Gene-commentary_type");
			if (typeElement1.getAttribute("value") != null && typeElement1.getAttributeValue("value").equals("comment")) {
				Element headerElement = result.getChild("Gene-commentary_heading");
				if (headerElement != null && headerElement.getText().equalsIgnoreCase("NCBI Reference Sequences (RefSeq)")) {
					Element commentElement = result.getChild("Gene-commentary_comment");
					List<Element> commentChildren = commentElement.getChildren("Gene-commentary");
					for (Element celement : commentChildren) {
						Element heading = celement.getChild("Gene-commentary_heading");
						if (heading != null && heading.getText().equalsIgnoreCase("RefSeqs maintained independently of Annotated Genomes")) {
							List<Element> products = celement.getChildren("Gene-commentary_products");
							for (Element element : products) {
								List<Element> refSeqItems = element.getChildren("Gene-commentary");
								for (Element element2 : refSeqItems) {
									Element typeElement = element2.getChild("Gene-commentary_type");
									if (typeElement.getAttribute("value") != null && typeElement.getAttributeValue("value").equals("mRNA")) {
										Element accessionElement = element2.getChild("Gene-commentary_accession");
										if (!mRNARefSeqList.contains(accessionElement.getText()))
											mRNARefSeqList.add(accessionElement.getText());
									}
									
									List<Element> proteinProducts = element2.getChildren("Gene-commentary_products");
									for (Element element3 : proteinProducts) {
										List<Element> refSeqItems2 = element3.getChildren("Gene-commentary");
										for (Element element4 : refSeqItems2) {
											typeElement = element4.getChild("Gene-commentary_type");
											if (typeElement.getAttribute("value") != null && typeElement.getAttributeValue("value").equals("peptide")) {
												Element accessionElement = element4.getChild("Gene-commentary_accession");
												if (!proteinRefSeqList.contains(accessionElement.getText()))
													proteinRefSeqList.add(accessionElement.getText());
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		
		//Full Name & symbol
		xpath = XPath.newInstance("//Entrezgene_properties/Gene-commentary/Gene-commentary_properties");
		resultList = xpath.selectNodes( t_childgene.get(0));
		for (Element element : resultList) {
			List<Element> properties = element.getChildren("Gene-commentary");
			for (Element element2 : properties) {
				Element labelElement = element2.getChild("Gene-commentary_label");
				if (labelElement != null && labelElement.getText().equals("Official Full Name")) {
					Element fullNameElement = element2.getChild("Gene-commentary_text");
					if (fullNameElement != null) {
						gene.setFullName(fullNameElement.getText());
					}
				}
				if (labelElement != null && labelElement.getText().equals("Official Symbol")) {
					Element symbolElement = element2.getChild("Gene-commentary_text");
					if (symbolElement != null) {
						gene.setGeneSymbol(symbolElement.getText());
					}
				}
			}
		}
		
		gene.setAliases(aliasList);
		gene.setRefSeq(mRNARefSeqList);
		gene.setSecondaryRefSeq (proteinRefSeqList);
		gene.setLocations(locations);
			
		return true;
	}

	/**
     * Perform an HTTP request and return the answer content.
     * @param a_url URL of the HTTP request
     * @return Content of the answer (XML).
     * @throws Exception
     */
	private String makeHttpRequest(URL a_url) throws Exception 
    {                 
        // read result
        URLConnection t_connection = a_url.openConnection();
        t_connection.setUseCaches(false); 

        BufferedReader t_reader = new BufferedReader(new InputStreamReader(t_connection.getInputStream()));
        int t_count;
        StringBuilder t_result = new StringBuilder();
        while( (t_count = t_reader.read())!= -1 ) 
        {
            t_result.appendCodePoint(t_count);
        }
        return t_result.toString();
    }
	
	public static void main(String[] args) {
		NCBIGeneUtil util = new NCBIGeneUtil();
		try {
			Integer geneId = util.getGeneIdFromNCBI("UGT3A1", "human");
			Gene gene = util.getDetailsFromNCBI(geneId);
			System.out.println(gene.getRefSeqString() + "\n" + gene.getSecondaryRefSeqString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
