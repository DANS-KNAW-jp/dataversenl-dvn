/**
 * Copyright (C) 2015-2016 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.dataverse;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * RuleServiceBean.java
 * 
 * @author Eko Indarto
*/
@Stateless
public class RuleServiceBean implements RuleServiceLocal {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3041567495764689270L;
	@PersistenceContext(unitName = "VDCNet-ejbPU")
	private EntityManager em;

	/**
	 * Creates a new instance of UserServiceBean
	 */
	public RuleServiceBean() {
	}

	@Override
	public List<Rule> findAll() {
		return em.createQuery("select object(o) from Rule as o")
				.getResultList();
	}

	@Override
	public Rule findRuleById(Long id) {
		return em.createQuery(
				"select object(o) from Rule as o where o.id=" + id, Rule.class)
				.getSingleResult();
	}

	@Override
	public Rule fineRuleByConditions(RuleCondition rc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Rule> findRuleByOrgName(String orgName) {
		return em.createQuery(
				"select object(o) from Rule as o where o.orgName='" + orgName
						+ "'").getResultList();
	}

}
