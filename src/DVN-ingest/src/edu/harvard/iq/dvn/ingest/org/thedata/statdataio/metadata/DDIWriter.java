/*
   Copyright (C) 2005-2012, by the President and Fellows of Harvard College.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Dataverse Network - A web application to share, preserve and analyze research data.
   Developed at the Institute for Quantitative Social Science, Harvard University.
   Version 3.0.
*/
package edu.harvard.iq.dvn.ingest.org.thedata.statdataio.metadata;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A writer that renders a <code>SDIOMetadata</code> object in 
 * <a href="http://www.ddialliance.org/">Data Documentation Initiative
 * </a>(DDI) format.
 * 
 * @author Akio Sone at UNC-Odum
 */
public class DDIWriter {


    /**
     * A <code>SDIOMetadata</code> object to be rendered in DDI format
     * 
     */
    protected SDIOMetadata sdioMetadata;

    /**
     * The default missing value string ('\u002E').
     */
    public String MISSING_VALUE_TOKEN =".";

    /**
     * The missing value (Long.MAX_VALUE) for a discrete variable
     * as <code>String</code>
     */
    public String MISSING_VALUE_DISCRETE ="9223372036854775807";

    /**
     * Sets the value of MISSING_VALUE_TOKEN
     * @param userDefinedMissingValue user-selected missing value
     */
    public void setMISSING_VALUE_TOKEN(String userDefinedMissingValue) {
        this.MISSING_VALUE_TOKEN = userDefinedMissingValue;
    }


    static {

    }

    /**
     * Constructs  a DDIWriter instance with the given <code>SDIOMetadata</code> object.
     * 
     * @param sdioMetadata
     */
    public DDIWriter(SDIOMetadata sdioMetadata) {
        this.sdioMetadata = sdioMetadata;
//        init();
    }

    /**
     * Renders the given <code>SDIOMetadata</code> object as a <code>String</code>
     * according to the DDI specification 2.0.
     * 
     * @return an XML representation of the <code>SDIOMetadata</code> object.
     */
    public String generateDDI(){
        StringBuilder sb = new StringBuilder();
        sb.append(generateDDISection1());
        //sb.append(generateDDISection2());
        sb.append(generateDDISection3());
        sb.append(generateDDISection4());
        
        return sb.toString();
    }

    private String generateDDISection1(){

        String defaultEncoding = "UTF-8";

        String fileEncoding = (String)sdioMetadata.fileInformation.get("charset");
        

        if(sdioMetadata.fileInformation.get("charset")== null){
            fileEncoding ="";
        }

        String encoding_attr = !fileEncoding.equals("") ?
                       " encoding=\""+fileEncoding+"\"" :
                       " encoding=\""+defaultEncoding+"\"";

        StringBuilder sb =
            new StringBuilder( "<?xml version=\"1.0\"" + encoding_attr +"?>\n");
        sb.append("<codeBook xmlns=\"http://www.icpsr.umich.edu/DDI\">\n");
        return sb.toString();
    }

    private String generateDDISection2(){
        StringBuilder sb = new StringBuilder(
            "<docDscr/>\n"+
            "<stdyDscr>\n\t<citation>\n\t\t<titlStmt>\n"+
            "\t\t\t<titl/>\n\t\t\t<IDNo agency=\"\"/>\n\t\t</titlStmt>\n"+
            "\t</citation>\n</stdyDscr>\n");
        return sb.toString();
    }

    private String generateDDISection3(){
        String charset = (String)sdioMetadata.fileInformation.get("charset");
        String nobs = sdioMetadata.fileInformation.get("caseQnty").toString();
        String nvar = sdioMetadata.fileInformation.get("varQnty").toString();
        String mimeType = (String)sdioMetadata.fileInformation.get("mimeType");

        //System.out.println("charset="+charset);
        //System.out.println("nobs="+nobs);
        //System.out.println("nvar="+nvar);
        //System.out.println("mimeType="+mimeType);
        //String recPrCas = (String)getFileInformation().get("recPrCas");
        //String fileType = (String)getFileInformation().get("fileType");
        //String fileFormat = (String)getFileInformation().get("fileFormat");

        String recPrCasTag = "";
        String fileFormatTag ="";

        String fileUNF = (String)sdioMetadata.getFileInformation().get("fileUNF");
        //System.out.println("fileUNF="+fileUNF);
        String fileNoteUNF =
            "\t<notes subject=\"Universal Numeric Fingerprint\" level=\"file\" "+
            "source=\"archive\" type=\"VDC:UNF\">" + fileUNF + "</notes>\n";

        String fileNoteFileType =
            "\t<notes subject=\"original file format\" level=\"file\" "+
            "source=\"archive\" type=\"VDC:MIME\">" +  mimeType + "</notes>\n";

        StringBuilder sb =
            new StringBuilder("<fileDscr ID=\"file1\" URI=\"\">\n" + "\t<fileTxt>\n"+
        "\t\t<dimensns>\n\t\t\t<caseQnty>"+nobs+"</caseQnty>\n"+
        "\t\t\t<varQnty>" + nvar +"</varQnty>\n"+
        recPrCasTag + "\t\t</dimensns>\n"+
        "\t\t<fileType charset=\""+ charset + "\">" + mimeType +"</fileType>\n"+
        fileFormatTag+"\t</fileTxt>\n"+
        fileNoteUNF+ fileNoteFileType+"</fileDscr>\n");

        return sb.toString();
    }
    private String generateDDISection4(){
        // String[] variableName
        // String[] variableLabel
        //
        String varFormatSchema = (String)sdioMetadata.fileInformation.get("varFormat_schema");
        String[] sumStatLabels8 =
             {"mean", "medn", "mode", "vald", "invd", "min", "max", "stdev"};
        StringBuilder sb = new StringBuilder("<dataDscr>\n");

        String[] sumStatLabels3 = {"vald", "invd", "mode"};
        
        
        sdioMetadata.getValueLabelMappingTable();
        
        

        
        
        boolean hasCaseWeightVariable = false;
        int caseWeightVariableIndex = 0;
        String wgtVarAttr = null;
        if ((sdioMetadata.caseWeightVariableName != null) &&
            (!sdioMetadata.caseWeightVariableName.equals(""))){
            hasCaseWeightVariable = true;
            for (int iw = 0; iw < sdioMetadata.variableName.length; iw++){
                if (sdioMetadata.variableName[iw].equals(sdioMetadata.caseWeightVariableName)){
                    caseWeightVariableIndex = iw;
                }
            }
            wgtVarAttr = "wgt-var=\"v1."+ (caseWeightVariableIndex+1)  +"\" ";
        } else {
            wgtVarAttr="";
        }
        String uncessaryZeros = "([\\+\\-]?[0-9]+)(\\.0*$)";
        Pattern pattern = Pattern.compile(uncessaryZeros);
        for (int i=0; i<sdioMetadata.variableName.length;i++){

            // prepare catStat
            String variableNamei = sdioMetadata.variableName[i];
            
            
            String valueLabelTableName = sdioMetadata.valueLabelMappingTable.get(variableNamei);
            
            // valuLabeli, catStati, missingValuei
             List<CategoricalStatistic> mergedCatStatTable =
                MetadataHelper.getMergedResult(
                sdioMetadata.valueLabelTable.get(valueLabelTableName),
                sdioMetadata.categoryStatisticsTable.get(variableNamei),
                sdioMetadata.missingValueTable.get(variableNamei),
                sdioMetadata.nullValueCounts.get(variableNamei)
                );
                
            // <var tag

            String intrvlType = sdioMetadata.isContinuousVariable()[i]
                     ? "contin": "discrete" ;

            String intrvlAttr = "intrvl=\""+intrvlType + "\" " ;
            String weightAttr = null;
            if (hasCaseWeightVariable) {
                if (i == caseWeightVariableIndex){
                    // weight variable's token
                    weightAttr = "wgt=\"wgt\" ";
                } else {
                    // non-weight variable's token
                    weightAttr = wgtVarAttr;
                }
            } else {
               
                weightAttr = "";
            }

            String ultimateVariableName = getUtimateVariableName(sdioMetadata.variableName[i]);
 
            sb.append("\t<var ID=\"v1." + (i+1) + "\" name=\"" +
                 StringEscapeUtils.escapeXml(ultimateVariableName) + "\" "+
                 intrvlAttr + weightAttr+">\n");  // id counter starst from 1 not 0

            sb.append("\t\t<location fileid=\"file1\"/>\n");

            // label
            if ((sdioMetadata.variableLabel.containsKey(sdioMetadata.variableName[i])) &&
                (!sdioMetadata.variableLabel.get(sdioMetadata.variableName[i]).equals(""))) {
                sb.append("\t\t<labl level=\"variable\">" +
                    StringEscapeUtils.escapeXml(
                    sdioMetadata.variableLabel.get(sdioMetadata.variableName[i]))+"</labl>\n");
            }

            if((sdioMetadata.invalidDataTable !=null) &&
                    (sdioMetadata.invalidDataTable.containsKey(sdioMetadata.variableName[i]))){
                sb.append(sdioMetadata.invalidDataTable.get(sdioMetadata.variableName[i]).toDDItag());
            }

            // summaryStatistics
            Object[] sumStat = sdioMetadata.summaryStatisticsTable.get(i);
            if (sumStat.length == 3){
                for (int j=0; j<sumStat.length;j++){
                    String statistic = (sumStat[j].toString()).equals("NaN")
                        || (sumStat[j].toString()).equals("")
                        ? MISSING_VALUE_TOKEN : StringEscapeUtils.escapeXml(sumStat[j].toString());
//                    sb.append("\t\t<sumStat type=\""+
//                        sumStatLabels3[j]+"\">"+statistic+"</sumStat>\n");

                            String wholePart = null;
                            Matcher matcher = pattern.matcher(statistic);
                            if (matcher.find()){
                                wholePart = matcher.group(1);
                            } else{
                                wholePart = statistic;
                            }
                            sb.append("\t\t<sumStat type=\""+
                            sumStatLabels3[j]+"\">"+wholePart+"</sumStat>\n");


                }
            } else if (sumStat.length== 8) {

                for (int j=0; j<sumStat.length;j++){

//                    if (!sdioMetadata.isContinuousVariable()[i]){
//                        if ((j == 0)|| (j== 7)) {
//                            continue;
//                        }
//                    }


                    String statistic = (sumStat[j].toString()).equals("NaN")
                        || (sumStat[j].toString()).equals("")
                        ? MISSING_VALUE_TOKEN : sumStat[j].toString();

                    if (!sdioMetadata.isContinuousVariable()[i]){
                        // discrete case: remove the decimal point
                        // and the trailing zero(s)
                        if ((j != 0) && (j!=7)){

                            String wholePart = null;
                            Matcher matcher = pattern.matcher(statistic);
                            if (matcher.find()){
                                wholePart = matcher.group(1);
                            } else{
                                wholePart = statistic;
                            }
                            sb.append("\t\t<sumStat type=\""+
                            sumStatLabels8[j]+"\">"+wholePart+"</sumStat>\n");

                        } else {
                            sb.append("\t\t<sumStat type=\""+
                            sumStatLabels8[j]+"\">"+statistic+"</sumStat>\n");
                        }

                    } else {
                        // valid/invalid are integers
                        if ((j == 3) || (j == 4)){

                            String wholePart = null;
                            Matcher matcher = pattern.matcher(statistic);
                            if (matcher.find()){
                                wholePart = matcher.group(1);
                            } else{
                                wholePart = statistic;
                            }
                            sb.append("\t\t<sumStat type=\""+
                            sumStatLabels8[j]+"\">"+wholePart+"</sumStat>\n");

                        } else {
                            sb.append("\t\t<sumStat type=\""+
                            sumStatLabels8[j]+"\">"+statistic+"</sumStat>\n");
                        }
                    }

                }
            }
            // cat stat
            /*
                <catgry missing="N">
                    <catValu>2</catValu>
                    <labl level="category">JA,NEBEN 2</labl>
                    <catStat type="freq">16</catStat>
                </catgry>
            */
            boolean orderedCategoricalValues = false; 
            String fileFormat = (String)sdioMetadata.getFileInformation().get("fileFormat");
            if (fileFormat != null && fileFormat.equals("RDATA")) {
                if (sdioMetadata.getVariableMetaData(i).isOrderedFactor()) {
                    orderedCategoricalValues = true; 
                }
            }
            
            if ((mergedCatStatTable != null) && (!mergedCatStatTable.isEmpty())){

                for (CategoricalStatistic cs: mergedCatStatTable){
                    String categoryAttributes = "";
                    if (orderedCategoricalValues) {
                        int order = sdioMetadata.getVariableMetaData(i).getFactorLevelOrder(cs.getValue());
                        if (order > -1) {
                            categoryAttributes = " order=\""+order+"\"";
                        }
                    }
                    
                    // first line
                    if (cs.isMissingValue() ||
                        cs.getValue().equals(MISSING_VALUE_DISCRETE) ||
                        cs.getValue().equals(MISSING_VALUE_TOKEN) /*||
                         * Commented out the empty string, "", as a 
                         * missing value -- L.A., May 10 '13
                        cs.getValue().equals("")*/){
                        sb.append("\t\t<catgry missing=\"Y\""+categoryAttributes+">\n");
                    } else {
                        sb.append("\t\t<catgry"+categoryAttributes+">\n");
                    }
                    // value
                    String catStatValueString = null;
                    if (cs.getValue().equals(MISSING_VALUE_DISCRETE)
                        || cs.getValue().equals(MISSING_VALUE_TOKEN)
                        /*|| cs.getValue().equals("")
                         * * Commented out the empty string, "", as a 
                         * missing value -- L.A., May 10 '13
                         */
                        ){
                        catStatValueString=MISSING_VALUE_TOKEN;
                    } else {
                        catStatValueString= StringEscapeUtils.escapeXml(cs.getValue());
                    }
                    sb.append("\t\t\t<catValu>"+catStatValueString+"</catValu>\n");
                    // label
                    if ((cs.getLabel()!=null) && (!cs.getLabel().equals(""))){
                        sb.append("\t\t\t<labl level=\"category\">"+
                            StringEscapeUtils.escapeXml(cs.getLabel())+"</labl>\n");
                    }
                    // frequency

                    sb.append("\t\t\t<catStat type=\"freq\">"+cs.getFrequency()+"</catStat>\n");
                    sb.append("\t\t</catgry>\n");
                }
            }
            
            //System.out.println(StringUtils.join(sumStat,","));
            // format
            String formatTye = sdioMetadata.isStringVariable()[i] ? "character" : "numeric";
            String formatName = null;
            String formatCategory = null;
            String formatSchema = null;
            if ((sdioMetadata.variableFormatName.containsKey(variableNamei)) && 
                (  (sdioMetadata.variableFormatName.get(variableNamei)!=null)
                && (!sdioMetadata.variableFormatName.get(variableNamei).equals("")))){
                formatSchema = "schema=\""+varFormatSchema+"\" ";
                formatName= "formatname=\""+sdioMetadata.variableFormatName.get(variableNamei) +"\" ";
                formatCategory = "category=\""+sdioMetadata.variableFormatCategory.get(variableNamei) +"\"";
            } else {
                formatSchema="";
                formatName="";
                formatCategory ="";
            }
            if (sdioMetadata.isBooleanVariable()[i]) {
                sb.append("\t\t<varFormat type=\""+formatTye+"\" "+formatSchema+ formatName+formatCategory+">Boolean</varFormat>\n");
            } else {
                sb.append("\t\t<varFormat type=\""+formatTye+"\" "+formatSchema+ formatName+formatCategory+"/>\n");
            }
            // note: UNF
            sb.append("\t\t<notes subject=\"Universal Numeric Fingerprint\" "+
                "level=\"variable\" source=\"archive\" type=\"VDC:UNF\">"+
                StringEscapeUtils.escapeXml(sdioMetadata.variableUNF[i])
                +"</notes>\n");
            // closing
            sb.append("\t</var>\n");
        }
        sb.append("</dataDscr>\n");
        sb.append("</codeBook>\n");
        return sb.toString();
    }

    private String getUtimateVariableName(String variableName){
        String ultimateVariableName = null;
        if ((sdioMetadata.shortToLongVarialbeNameTable != null) &&
            (sdioMetadata.shortToLongVarialbeNameTable.containsKey(variableName))){
             if((sdioMetadata.shortToLongVarialbeNameTable.get(variableName) !=null) &&
                (!sdioMetadata.shortToLongVarialbeNameTable.get(variableName).equals(""))) {
                 ultimateVariableName = sdioMetadata.shortToLongVarialbeNameTable.get(variableName);
             } else {
                 ultimateVariableName = variableName;
             }
        } else {
            ultimateVariableName = variableName;
        }
        return ultimateVariableName;
    }
}
