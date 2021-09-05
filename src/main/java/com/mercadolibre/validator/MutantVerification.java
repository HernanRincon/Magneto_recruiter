package com.mercadolibre.validator;

import static com.mercadolibre.enums.MutantGeneEnum.MUTAN_GEN_A;
import static com.mercadolibre.enums.MutantGeneEnum.MUTAN_GEN_C;
import static com.mercadolibre.enums.MutantGeneEnum.MUTAN_GEN_G;
import static com.mercadolibre.enums.MutantGeneEnum.MUTAN_GEN_T;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;

import com.mercadolibre.model.ValidationDnaData;

@Default
@RequestScoped
public class MutantVerification {
	/**
	 * this method can verify if dna is human or mutant
	 * 
	 * @param dna
	 * @return
	 */
	public boolean isMutant(Optional<List<String>> dna) {
		List<String> diagonals = new ArrayList<String>();
		List<String> columns = new ArrayList<String>();
		List<List<String>> matrixDna = dna.orElse(new ArrayList<String>()).stream().map(
				ma -> ma.chars().mapToObj(m -> String.valueOf((char) m).toUpperCase()).collect(Collectors.toList()))
				.collect(Collectors.toList());
		int sizeMatrix = matrixDna.size();

		for (int i = 0; i < sizeMatrix; i++) {
			final int row = i;
			StringBuilder topDiagonals = new StringBuilder();
			StringBuilder topDiagonalsRevers = new StringBuilder();
			StringBuilder bottomDiagonals = new StringBuilder();
			StringBuilder bottomDiagonalsRevers = new StringBuilder();

			IntStream.range(0 + i, sizeMatrix).forEach(j -> topDiagonals.append(matrixDna.get(j - row).get(j)));
			if (topDiagonals.length() > 3) {
				diagonals.add(topDiagonals.toString());
			}
			IntStream.range(0 + i, sizeMatrix)
					.forEach(j -> topDiagonalsRevers.append(matrixDna.get(j - row).get(matrixDna.size() - 1 - j)));
			if (topDiagonalsRevers.length() > 3) {
				diagonals.add(topDiagonalsRevers.toString());
			}
			IntStream.range(+i, sizeMatrix).forEach(j -> bottomDiagonals.append(matrixDna.get(j).get(j - row)));
			if (bottomDiagonals.length() > 3) {
				if (i != 0) {
					diagonals.add(bottomDiagonals.toString());
				}
			}
			IntStream.range(0, sizeMatrix - i)
					.forEach(j -> bottomDiagonalsRevers.append(matrixDna.get(j + row).get(matrixDna.size() - 1 - j)));
			if (bottomDiagonalsRevers.length() > 3) {
				if (i != 0) {
					diagonals.add(bottomDiagonalsRevers.toString());
				}
			}
		}

		for (int j = 0; j < sizeMatrix; j++) {
			StringBuilder col = new StringBuilder();
			for (int i = 0; i < sizeMatrix; i++) {
				col.append(matrixDna.get(i).get(j));
			}
			columns.add(col.toString());
		}
		long countColumns = columns.parallelStream()
				.filter(fil -> fil.contains(MUTAN_GEN_A.getValue()) || fil.contains(MUTAN_GEN_C.getValue())
						|| fil.contains(MUTAN_GEN_G.getValue()) || fil.contains(MUTAN_GEN_T.getValue()))
				.count();
		long countDiagonals = diagonals.parallelStream()
				.filter(fil -> fil.contains(MUTAN_GEN_A.getValue()) || fil.contains(MUTAN_GEN_C.getValue())
						|| fil.contains(MUTAN_GEN_G.getValue()) || fil.contains(MUTAN_GEN_T.getValue()))
				.count();
		long countDna = dna.orElse(new ArrayList<String>()).parallelStream()
				.filter(fil -> fil.contains(MUTAN_GEN_A.getValue()) || fil.contains(MUTAN_GEN_C.getValue())
						|| fil.contains(MUTAN_GEN_G.getValue()) || fil.contains(MUTAN_GEN_T.getValue()))
				.count();
		return (countColumns + countDiagonals + countDna) > 1;
	}

	/**
	 * This method
	 * @param dna
	 * @return
	 */
	public ValidationDnaData verifyDna(Optional<List<String>> dna) {
		ValidationDnaData dnaData = new ValidationDnaData();
		dnaData.setMessage("DNA ok");
		dnaData.setValidDna(true);

		if (dna.isEmpty()) {
			dnaData.setMessage("DNA is empty");
			dnaData.setValidDna(false);
			return dnaData;
		}
		List<List<String>> matrixDna = dna.get().stream().map(
				ma -> ma.chars().mapToObj(m -> String.valueOf((char) m).toUpperCase()).collect(Collectors.toList()))
				.collect(Collectors.toList());
		boolean ValidDNANotation = matrixDna.stream().parallel().allMatch(
				p -> p.stream().allMatch(v ->  v.equals("A") || v.equals("C") || v.equals("G") || v.equals("T")));
		if (!ValidDNANotation) {
			dnaData.setMessage("DNA contains characters diferents to (A or C or G or T)");
			dnaData.setValidDna(false);
			return dnaData;
		}
		boolean isSimetrict=matrixDna.stream().parallel().allMatch(f-> f.size()==matrixDna.size());
		if (!isSimetrict) {
			dnaData.setMessage("DNA is not simetric the rows and columns should be the same size");
			dnaData.setValidDna(false);
			return dnaData;
		}

		return dnaData;
	}

}
