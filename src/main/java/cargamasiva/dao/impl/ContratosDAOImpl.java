package cargamasiva.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.bbva.arqspring.dao.BaseDAO;

import cargamasiva.dao.ContratosDAO;
import cargamasiva.dto.FideicomisarioDTO;
import cargamasiva.dto.FideicomitenteDTO;
import cargamasiva.util.ConstantesBatch;

@Repository
public class ContratosDAOImpl extends BaseDAO implements ContratosDAO {

	public List<FideicomisarioDTO> getCtoFideicomisario() {
		List<FideicomisarioDTO> result = new ArrayList<FideicomisarioDTO>();
		String formateado;
		String constants = "BEN_E_MAIL";

		try {
			StringBuilder sql = new StringBuilder();
			Map<String, Object> params = new HashMap<String, Object>();

			logger.info(":::-- Inicia QUERY getCtoFideicomisario --:::");

			// 'ZONA RESTRINGIDA'
			sql.append("SELECT FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, BEN_E_MAIL ").append("FROM ").append("(")
					.append("SELECT FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, NUM_CONTRATO, CTO_NOM_ACTIVIDAD ")
					.append("FROM").append("(")
					.append("SELECT NUM_CONTRATO, PC.SUB_CONTRATO, FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, CTO_NOM_ACTIVIDAD ")
					.append(", CASE WHEN (PC.ANO_VENCIMIENTO > YEAR (CURRENT DATE) OR (PC.ANO_VENCIMIENTO = YEAR (CURRENT DATE) AND PC.MES_VENCIMIENTO > MONTH (CURRENT DATE)) OR (PC.ANO_VENCIMIENTO = YEAR (CURRENT DATE) AND PC.MES_VENCIMIENTO = MONTH (CURRENT DATE) AND PC.DIA_VENCIMIENTO > DAY (CURRENT DATE))) THEN 'ACTIVO' ELSE 'VENCIDO' END AS ESTATUS ")
					.append("FROM").append("(")
					.append("SELECT C.CTO_NUM_CONTRATO AS NUM_CONTRATO, COALESCE(SC.SCT_SUB_CONTRATO, 0) AS SUB_CONTRATO, ")
					.append("DATE(LTRIM(RTRIM(CHAR(COALESCE(CTO_ANO_VENCIM,0))))||'-'||LTRIM(RTRIM(CHAR(COALESCE(CTO_MES_VENCIM,0))))||'-'||LTRIM(RTRIM(CHAR(COALESCE(CTO_DIA_VENCIM,0))))) AS FECHA_VENCIMIENTO, ")
					.append("CTO_ANO_VENCIM AS ANO_VENCIMIENTO, CTO_MES_VENCIM AS MES_VENCIMIENTO, CTO_DIA_VENCIM AS DIA_VENCIMIENTO, CTO_NOM_ACTIVIDAD, CTO_CVE_TIPO_NEG ")
					.append("FROM  GDB2PR.CONTRATO C ")
					.append("LEFT JOIN SUBCONT SC ON C.CTO_NUM_CONTRATO = SC.SCT_NUM_CONTRATO ")
					.append("WHERE COALESCE(CTO_ANO_VENCIM,0) > 0 AND COALESCE(CTO_MES_VENCIM,0) > 0 AND COALESCE(CTO_DIA_VENCIM,0) > 0 AND CTO_NOM_ACTIVIDAD = 'ZONA RESTRINGIDA' ")
					.append("ORDER BY 3, 1, 2 ").append(") PC").append("ORDER BY 3, 1, 2").append(") S ")
					.append("LEFT JOIN FDPACAHON HON ON S.NUM_CONTRATO = HON.PAC_NUM_CONTRATO AND HON.PAC_NUM_SUBFISO = S.SUB_CONTRATO ")
					.append("WHERE  S.ESTATUS = 'VENCIDO' AND S.NUM_CONTRATO NOT IN (SELECT DISTINCT CB.CTB_CONTRATO FROM GDB2PR.CTOBLOQU CB WHERE S.NUM_CONTRATO = CB.CTB_CONTRATO AND S.SUB_CONTRATO = CB.CTB_SUB_CONTRATO) AND HON.PAC_CVE_ST_PACAHON <> 'EN TRAMITE DE EXTINCION' ")
					.append(")")
					.append("INNER JOIN GDB2PR.BENEFICI ON BEN_NUM_CONTRATO = NUM_CONTRATO WHERE BEN_CVE_ST_BENEFIC = 'ACTIVO' AND BEN_E_MAIL LIKE '%@%' GROUP BY FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, BEN_E_MAIL WITH UR");

			logger.info(ConstantesBatch.QUERY + sql);
			logger.info(ConstantesBatch.PARAM + params.toString());

			List<Map<String, Object>> queryMap = jdbcTemplate.queryForList(sql.toString(),
					new MapSqlParameterSource(params));
			if (null != queryMap && !queryMap.isEmpty()) {
				for (Map<String, Object> query : queryMap) {
					FideicomisarioDTO ctoFideicomisario = new FideicomisarioDTO();
					ctoFideicomisario.setFechaVencimiento((java.sql.Date) query.get("FECHA_VENCIMIENTO"));
					ctoFideicomisario.setCveTipoNegocio((String) query.get("CTO_CVE_TIPO_NEG"));
					if (validarCorreo((String) query.get(constants))) {
						ctoFideicomisario.setCorreo((String) query.get(constants));
						formateado = String.format("%-300s", query.get("BEN_E_MAIL"));
						ctoFideicomisario.setCorreo(formateado);
						result.add(ctoFideicomisario);
					}
				}
				
			}
		} catch (Exception e) {
			logger.error("Error al recuperar info " + e);
		}
		return result;
	}

	public List<FideicomitenteDTO> getCtoFideicomitente() {
		List<FideicomitenteDTO> result = new ArrayList<FideicomitenteDTO>();
		String textoFormateado;
		String constant = "AFB_E_MAIL";

		try {
			StringBuilder sql = new StringBuilder();
			Map<String, Object> params = new HashMap<String, Object>();

			logger.info(":::-- Inicia QUERY getCtoFideicomitente --:::");

			// 'NOT ZONA RESTRINGIDA'
			sql.append("SELECT FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, AFB_E_MAIL").append(" FROM ").append("( ")
					.append("SELECT FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, NUM_CONTRATO, CTO_NOM_ACTIVIDAD")
					.append("FROM").append("(")
					.append("SELECT NUM_CONTRATO, PC.SUB_CONTRATO, FECHA_VENCIMIENTO, CTO_CVE_TIPO_NEG, CTO_NOM_ACTIVIDAD ")
					.append(", CASE WHEN (PC.ANO_VENCIMIENTO > YEAR (CURRENT DATE) OR (PC.ANO_VENCIMIENTO = YEAR (CURRENT DATE) AND PC.MES_VENCIMIENTO > MONTH (CURRENT DATE)) OR (PC.ANO_VENCIMIENTO = YEAR (CURRENT DATE) AND PC.MES_VENCIMIENTO = MONTH (CURRENT DATE) AND PC.DIA_VENCIMIENTO > DAY (CURRENT DATE))) THEN 'ACTIVO' ELSE 'VENCIDO' END AS ESTATUS ")
					.append("FROM ").append("( ")
					.append("SELECT C.CTO_NUM_CONTRATO AS NUM_CONTRATO, COALESCE(SC.SCT_SUB_CONTRATO, 0) AS SUB_CONTRATO, ")
					.append("DATE(LTRIM(RTRIM(CHAR(COALESCE(CTO_ANO_VENCIM,0))))||'-'||LTRIM(RTRIM(CHAR(COALESCE(CTO_MES_VENCIM,0))))||'-'||LTRIM(RTRIM(CHAR(COALESCE(CTO_DIA_VENCIM,0))))) AS FECHA_VENCIMIENTO, ")
					.append("CTO_ANO_VENCIM AS ANO_VENCIMIENTO, CTO_MES_VENCIM AS MES_VENCIMIENTO, CTO_DIA_VENCIM AS DIA_VENCIMIENTO, CTO_NOM_ACTIVIDAD, CTO_CVE_TIPO_NEG ")
					.append("FROM  GDB2PR.CONTRATO C ")
					.append("LEFT JOIN SUBCONT SC ON C.CTO_NUM_CONTRATO = SC.SCT_NUM_CONTRATO ")
					.append("WHERE COALESCE(CTO_ANO_VENCIM,0) > 0 AND COALESCE(CTO_MES_VENCIM,0) > 0 AND COALESCE(CTO_DIA_VENCIM,0 ) > 0 AND CTO_NOM_ACTIVIDAD = 'ZONA RESTRINGIDA'")
					.append("ORDER BY 3, 1, 2 ").append(") PC ").append("ORDER BY 3, 1, 2 ").append(") S ")
					.append("LEFT JOIN FDPACAHON HON ON S.NUM_CONTRATO = HON.PAC_NUM_CONTRATO AND HON.PAC_NUM_SUBFISO = S.SUB_CONTRATO ")
					.append("WHERE  S.ESTATUS = 'VENCIDO' AND S.NUM_CONTRATO NOT IN (SELECT DISTINCT CB.CTB_CONTRATO FROM GDB2PR.CTOBLOQU CB WHERE S.NUM_CONTRATO = CB.CTB_CONTRATO AND S.SUB_CONTRATO = CB.CTB_SUB_CONTRATO) AND HON.PAC_CVE_ST_PACAHON <> 'EN TRAMITE DE EXTINCION' ")
					.append(") INNER JOIN GDB2PR.FIDEICOM ON FID_NUM_CONTRATO = NUM_CONTRATO WHERE FID_CVE_ST_FIDEICO = 'ACTIVO' AND AFB_E_MAIL LIKE '%@%' GROUP BY AFB_E_MAIL, CTO_CVE_TIPO_NEG, FECHA_VENCIMIENTO WITH UR");

			logger.info(ConstantesBatch.QUERY + sql);
			logger.info(ConstantesBatch.PARAM + params.toString());

			List<Map<String, Object>> queryMap = jdbcTemplate.queryForList(sql.toString(),
					new MapSqlParameterSource(params));

			if (null != queryMap && !queryMap.isEmpty()) {
				for (Map<String, Object> query : queryMap) {
					FideicomitenteDTO ctoFideicomitente = new FideicomitenteDTO();
					ctoFideicomitente.setFechaVencimiento((java.sql.Date) query.get("FECHA_VENCIMIENTO"));
					ctoFideicomitente.setCveTipoNegocio((String) query.get("CTO_CVE_TIPO_NEG"));

					if (validarCorreo((String) query.get(constant))) {
						ctoFideicomitente.setCorreo((String) query.get(constant));
						textoFormateado = String.format("%-300s", query.get("AFB_E_MAIL"));
						ctoFideicomitente.setCorreo(textoFormateado);
						result.add(ctoFideicomitente);
					}
					jhkjkj
				}
			}
		} catch (Exception e) {
			logger.error("Error al recuperar info:: " + e);
		}
		return result;
	}

	public boolean validarCorreo(String email) {
		
		Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)@"+"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)(\\.[A-Za-z]{2,})$");
        Matcher mather = pattern.matcher(email.trim());
       
        if (mather.find()) {
        logger.error("Error al recuperar info: " + mather);
        return true;
	}
		return false;
	
}
}

