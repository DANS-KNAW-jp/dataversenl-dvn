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
package edu.harvard.iq.dvn.api.datadeposit;

import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.StudyServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCServiceLocal;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.swordapp.server.AuthCredentials;
import org.swordapp.server.CollectionDepositManager;
import org.swordapp.server.Deposit;
import org.swordapp.server.DepositReceipt;
import org.swordapp.server.SwordAuthException;
import org.swordapp.server.SwordConfiguration;
import org.swordapp.server.SwordError;
import org.swordapp.server.SwordServerException;

public class CollectionDepositManagerImpl implements CollectionDepositManager {

    private static final Logger logger = Logger.getLogger(CollectionDepositManagerImpl.class.getCanonicalName());
    @EJB
    VDCServiceLocal vdcService;
    @EJB
    StudyServiceLocal studyService;
    @Inject
    SwordAuth swordAuth;
    @Inject
    UrlManager urlManager;

    @Override
    public DepositReceipt createNew(String collectionUri, Deposit deposit, AuthCredentials authCredentials, SwordConfiguration config)
            throws SwordError, SwordServerException, SwordAuthException {

        VDCUser vdcUser = swordAuth.auth(authCredentials);

        urlManager.processUrl(collectionUri);
        String dvAlias = urlManager.getTargetIdentifier();
        if (urlManager.getTargetType().equals("dataverse") && dvAlias != null) {

            logger.info("attempting deposit into this dataverse alias: " + dvAlias);

            VDC dvThatWillOwnStudy = vdcService.findByAlias(dvAlias);

            if (dvThatWillOwnStudy != null) {

                List<VDC> userVDCs = vdcService.getUserVDCs(vdcUser.getId());
                if (swordAuth.hasAccessToModifyDataverse(vdcUser, dvThatWillOwnStudy)) {

                    if (userVDCs.size() != 1) {
                        throw new SwordServerException("the account used to modify a Journal Dataverse can only have access to 1 dataverse, not " + userVDCs.size());
                    }

                    logger.info("multipart: " + deposit.isMultipart());
                    logger.info("binary only: " + deposit.isBinaryOnly());
                    logger.info("entry only: " + deposit.isEntryOnly());
                    logger.info("in progress: " + deposit.isInProgress());
                    logger.info("metadata relevant: " + deposit.isMetadataRelevant());

                    if (deposit.isEntryOnly()) {
                        // require title *and* exercise the SWORD jar a bit
                        Map<String, List<String>> dublinCore = deposit.getSwordEntry().getDublinCore();
                        if (dublinCore.get("title") == null || dublinCore.get("title").get(0) == null) {
                            throw new SwordError("title field is required");
                        }

                        // instead of writing a tmp file, maybe importStudy() could accept an InputStream?
                        String tmpDirectory = config.getTempDirectory();
                        String uploadDirPath = tmpDirectory + File.separator + "import" + File.separator + dvThatWillOwnStudy.getId();
                        File uploadDir = new File(uploadDirPath);
                        if (!uploadDir.exists()) {
                            if (!uploadDir.mkdirs()) {
                                throw new SwordServerException("couldn't create directory: " + uploadDir.getAbsolutePath());
                            }
                        }
                        String tmpFilePath = uploadDirPath + File.separator + "newStudyViaSwordv2.xml";
                        File tmpFile = new File(tmpFilePath);
                        try {
                            FileUtils.writeStringToFile(tmpFile, deposit.getSwordEntry().getEntry().toString());
                        } catch (IOException ex) {
                            throw new SwordServerException("Could write temporary file");
                        } finally {
                            uploadDir.delete();
                        }

                        Long dcmiTermsFormatId = new Long(4);
                        Study study;
                        try {
                            study = studyService.importStudy(tmpFile, dcmiTermsFormatId, dvThatWillOwnStudy.getId(), vdcUser.getId());
                        } catch (Exception ex) {
                            throw new SwordError("Couldn't import study: " + ex.getMessage());
                        } finally {
                            tmpFile.delete();
                            uploadDir.delete();
                        }
                        ReceiptGenerator receiptGenerator = new ReceiptGenerator();
                        DepositReceipt depositReceipt = receiptGenerator.createReceipt(urlManager.getHostnamePlusBaseUrlPath(collectionUri), study);
                        return depositReceipt;
                    } else if (deposit.isBinaryOnly()) {
                        // get here with this:
                        // curl --insecure -s --data-binary "@example.zip" -H "Content-Disposition: filename=example.zip" -H "Content-Type: application/zip" https://sword:sword@localhost:8181/dvn/api/data-deposit/v1/swordv2/collection/dataverse/sword/
                        throw new SwordError("Binary deposit to the collection IRI via POST is not supported. Please POST an Atom entry instead.");
                    } else if (deposit.isMultipart()) {
                        // get here with this:
                        // wget https://raw.github.com/swordapp/Simple-Sword-Server/master/tests/resources/multipart.dat
                        // curl --insecure --data-binary "@multipart.dat" -H 'Content-Type: multipart/related; boundary="===============0670350989=="' -H "MIME-Version: 1.0" https://sword:sword@localhost:8181/dvn/api/data-deposit/v1/swordv2/collection/dataverse/sword/hdl:1902.1/12345
                        // but...
                        // "Yeah, multipart is critically broken across all implementations" -- http://www.mail-archive.com/sword-app-tech@lists.sourceforge.net/msg00327.html
                        throw new UnsupportedOperationException("Not yet implemented");
                    } else {
                        throw new SwordError("expected deposit types are isEntryOnly, isBinaryOnly, and isMultiPart");
                    }
                } else {
                    throw new SwordError("user " + vdcUser.getUserName() + " is not authorized to modify study");
                }
            } else {
                throw new SwordServerException("Could not find dataverse: " + dvAlias);
            }
        } else {
            throw new SwordError("Could not determine target type or identifier from url: " + collectionUri);
        }
    }
}
