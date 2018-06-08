package test

/**
 * @filename:       KnapsackProblem.java
 * @author:         Matthew Mayo
 * @modified:       2014-04-08
 * @description:    Creates a KnapsackProblem object based on user input,
 * attempts to solve using a genetic algorithm; outputs
 * algorithm data step-by-step, generates list of optimal
 * items for problem, graphs mean fitness by generation;
 * optional command line argument output_filename will
 * redirect all algorithm details output to output_filename
 * in current directory, will overwrite output_filename
 * contents if file exists
 * @usage:          java KnapsackProblem <output_filename>
</output_filename> */

import java.io.Console
import java.io.File
import java.io.FileOutputStream
import java.io.FileNotFoundException
import java.io.PrintStream
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.Random

class KnapsackProblem {

    private val verbose = false
    private var mutation = false
    private var crossover_count = 0
    private var clone_count = 0
    private var number_of_items = 0
    private var population_size = 0
    private var maximum_generations = 0
    private var generation_counter = 1
    private var knapsack_capacity = 0.0
    private var prob_crossover = 0.0
    private var prob_mutation = 0.0
    private var total_fitness_of_generation = 0.0
    private val value_of_items = ArrayList<Double>()
    private val weight_of_items = ArrayList<Double>()
    private val fitness = ArrayList<Double>()
    private val best_fitness_of_generation = ArrayList<Double>()
    private val mean_fitness_of_generation = ArrayList<Double>()
    private val population = ArrayList<String>()
    private val breed_population = ArrayList<String>()
    private val best_solution_of_generation = ArrayList<String>()

    /**
     * Gets best solution in population
     * @return int - position of best solution in population
     */
    private val bestSolution: Int
        get() {
            var best_position = 0
            var this_fitness = 0.0
            var best_fitness = 0.0
            for (i in 0 until population_size) {
                this_fitness = evalGene(population[i])
                if (this_fitness > best_fitness) {
                    best_fitness = this_fitness
                    best_position = i
                }
            }
            return best_position
        }

    /**
     * Gets mean fitness of generation
     */
    private val meanFitness: Double
        get() {
            var total_fitness = 0.0
            var mean_fitness = 0.0
            for (i in 0 until population_size) {
                total_fitness = total_fitness + fitness[i]
            }
            mean_fitness = total_fitness / population_size
            return mean_fitness
        }

    /**
     * Default constructor
     */
    init {

        // Get user input
        this.getInput()

        // Make first generation
        this.buildKnapsackProblem()

        // Output summary
        this.showOptimalList()
    }

    /**
     * Controls knapsack problem logic and creates first generation
     */
    fun buildKnapsackProblem() {

        // Generate initial random population (first generation)
        this.makePopulation()

        // Start printing out summary
        println("\nInitial Generation:")
        println("===================")
        println("Population:")
        for (i in 0 until this.population_size) {
            println((i + 1).toString() + " - " + this.population[i])
        }

        // Evaluate fitness of initial population members
        this.evalPopulation()

        // Output fitness summary
        println("\nFitness:")
        for (i in 0 until this.population_size) {
            println((i + 1).toString() + " - " + this.fitness[i])
        }

        // Find best solution of generation
        this.best_solution_of_generation.add(this.population[this.bestSolution])

        // Output best solution of generation
        println("\nBest solution of initial generation: " + this.best_solution_of_generation[0])

        // Find mean solution of generation
        this.mean_fitness_of_generation.add(this.meanFitness)

        // Output mean solution of generation
        println("Mean fitness of initial generation: " + this.mean_fitness_of_generation[0])

        // Compute fitness of best solution of generation
        this.best_fitness_of_generation.add(this.evalGene(this.population[this.bestSolution]))

        // Output best fitness of generation
        println("Fitness score of best solution of initial generation: " + this.best_fitness_of_generation[0])

        // If maximum_generations > 1, breed further generations
        if (this.maximum_generations > 1) {
            makeFurtherGenerations()
        }
    }

    /**
     * Makes further generations beyond first, if necessary
     */
    private fun makeFurtherGenerations() {

        // Breeding loops maximum_generation number of times at most
        for (i in 1 until this.maximum_generations) {

            // Check for stopping criterion
            if (this.maximum_generations > 4 && i > 4) {

                // Previous 3 generational fitness values
                val a = this.mean_fitness_of_generation[i - 1]
                val b = this.mean_fitness_of_generation[i - 2]
                val c = this.mean_fitness_of_generation[i - 3]

                // If all are 3 equal, stop
                if (a == b && b == c) {
                    println("\nStop criterion met")
                    maximum_generations = i
                    break
                }
            }

            // Reset some counters
            this.crossover_count = 0
            this.clone_count = 0
            this.mutation = false

            // Breed population
            for (j in 0 until this.population_size / 2) {
                this.breedPopulation()
            }

            // Clear fitness values of previous generation
            this.fitness.clear()

            // Evaluate fitness of breed population members
            this.evalBreedPopulation()

            // Copy breed_population to population
            for (k in 0 until this.population_size) {
                this.population[k] = this.breed_population[k]
            }

            // Output population
            println("\nGeneration " + (i + 1) + ":")
            if (i + 1 < 10) {
                println("=============")
            }
            if (i + 1 >= 10) {
                println("==============")
            }
            if (i + 1 >= 100) {
                println("===============")
            }
            println("Population:")
            for (l in 0 until this.population_size) {
                println((l + 1).toString() + " - " + this.population[l])
            }

            // Output fitness summary
            println("\nFitness:")
            for (m in 0 until this.population_size) {
                println((m + 1).toString() + " - " + this.fitness[m])
            }

            // Clear breed_population
            this.breed_population.clear()

            // Find best solution of generation
            this.best_solution_of_generation.add(this.population[this.bestSolution])

            // Output best solution of generation
            println("\nBest solution of generation " + (i + 1) + ": " + this.best_solution_of_generation[i])

            // Find mean solution of generation
            this.mean_fitness_of_generation.add(this.meanFitness)

            // Output mean solution of generation
            println("Mean fitness of generation: " + this.mean_fitness_of_generation[i])

            // Compute fitness of best solution of generation
            this.best_fitness_of_generation.add(this.evalGene(this.population[this.bestSolution]))

            // Output best fitness of generation
            println("Fitness score of best solution of generation " + (i + 1) + ": " + this.best_fitness_of_generation[i])

            // Output crossover/cloning summary
            println("Crossover occurred " + this.crossover_count + " times")
            println("Cloning occurred " + this.clone_count + " times")
            if (this.mutation == false) {
                println("Mutation did not occur")
            }
            if (this.mutation == true) {
                println("Mutation did occur")
            }
        }
    }

    /**
     * Output KnapsackProblem summary
     */
    private fun showOptimalList() {

        // Output optimal list of items
        println("\nOptimal list of items to include in knapsack: ")

        var best_fitness = 0.0
        var best_gen = 0

        // First, find best solution out of generational bests
        for (z in 0 until this.maximum_generations - 1) {
            if (this.best_fitness_of_generation[z] > best_fitness) {
                best_fitness = this.best_fitness_of_generation[z]
                best_gen = z
            }
        }

        // Then, go through that's generation's best solution and output items
        val optimal_list = this.best_solution_of_generation[best_gen]
        for (y in 0 until this.number_of_items) {
            if (optimal_list.substring(y, y + 1) == "1") {
                print((y + 1).toString() + " ")
            }
        }
    }

    /**
     * Breeds current population to create a new generation's population
     */
    private fun breedPopulation() {

        // 2 genes for breeding
        val gene_1: Int
        val gene_2: Int

        // Increase generation_counter
        generation_counter = generation_counter + 1

        // If population_size is odd #, use elitism to clone best solution of previous generation
        if (population_size % 2 == 1) {
            breed_population.add(best_solution_of_generation[generation_counter - 1])
        }

        // Get positions of pair of genes for breeding
        gene_1 = selectGene()
        gene_2 = selectGene()

        // Crossover or cloning
        crossoverGenes(gene_1, gene_2)
    }

    /**
     * Performs mutation, if necessary
     */
    private fun mutateGene() {

        // Decide if mutation is to be used
        val rand_mutation = Math.random()
        if (rand_mutation <= prob_mutation) {

            // If so, perform mutation
            mutation = true
            var mut_gene: String
            var new_mut_gene: String
            val generator = Random()
            var mut_point = 0
            val which_gene = Math.random() * 100

            // Mutate gene
            if (which_gene <= 50) {
                mut_gene = breed_population[breed_population.size - 1]
                mut_point = generator.nextInt(number_of_items)
                if (mut_gene.substring(mut_point, mut_point + 1) == "1") {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "0" + mut_gene.substring(mut_point)
                    breed_population[breed_population.size - 1] = new_mut_gene
                }
                if (mut_gene.substring(mut_point, mut_point + 1) == "0") {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "1" + mut_gene.substring(mut_point)
                    breed_population[breed_population.size - 1] = new_mut_gene
                }
            }
            if (which_gene > 50) {
                mut_gene = breed_population[breed_population.size - 2]
                mut_point = generator.nextInt(number_of_items)
                if (mut_gene.substring(mut_point, mut_point + 1) == "1") {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "0" + mut_gene.substring(mut_point)
                    breed_population[breed_population.size - 1] = new_mut_gene
                }
                if (mut_gene.substring(mut_point, mut_point + 1) == "0") {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "1" + mut_gene.substring(mut_point)
                    breed_population[breed_population.size - 2] = new_mut_gene
                }
            }
        }
    }

    /**
     * Selects a gene for breeding
     * @return int - position of gene in population ArrayList to use for breeding
     */
    private fun selectGene(): Int {

        // Generate random number between 0 and total_fitness_of_generation
        var rand = Math.random() * total_fitness_of_generation

        // Use random number to select gene based on fitness level
        for (i in 0 until population_size) {
            if (rand <= fitness[i]) {
                return i
            }
            rand = rand - fitness[i]
        }

        // Not reachable; default return value
        return 0
    }

    /**
     * Performs either crossover or cloning
     */
    private fun crossoverGenes(gene_1: Int, gene_2: Int) {

        // Strings to hold new genes
        val new_gene_1: String
        val new_gene_2: String

        // Decide if crossover is to be used
        val rand_crossover = Math.random()
        if (rand_crossover <= prob_crossover) {
            // Perform crossover
            crossover_count = crossover_count + 1
            val generator = Random()
            val cross_point = generator.nextInt(number_of_items) + 1

            // Cross genes at random spot in strings
            new_gene_1 = population[gene_1].substring(0, cross_point) + population[gene_2].substring(cross_point)
            new_gene_2 = population[gene_2].substring(0, cross_point) + population[gene_1].substring(cross_point)

            // Add new genes to breed_population
            breed_population.add(new_gene_1)
            breed_population.add(new_gene_2)
        } else {
            // Otherwise, perform cloning
            clone_count = clone_count + 1
            breed_population.add(population[gene_1])
            breed_population.add(population[gene_2])
        }

        // Check if mutation is to be performed
        mutateGene()
    }

    /**
     * Evaluates entire population's fitness, by filling fitness ArrayList
     * with fitness value of each corresponding population member gene
     */
    private fun evalPopulation() {
        total_fitness_of_generation = 0.0
        for (i in 0 until population_size) {
            val temp_fitness = evalGene(population[i])
            fitness.add(temp_fitness)
            total_fitness_of_generation = total_fitness_of_generation + temp_fitness
        }
    }

    /**
     * Evaluates entire breed_population's fitness, by filling breed_fitness ArrayList
     * with fitness value of each corresponding breed_population member gene
     */
    private fun evalBreedPopulation() {
        total_fitness_of_generation = 0.0
        for (i in 0 until population_size) {
            val temp_fitness = evalGene(breed_population[i])
            fitness.add(temp_fitness)
            total_fitness_of_generation = total_fitness_of_generation + temp_fitness
        }
    }

    /**
     * Evaluates a single gene's fitness, by calculating the total_weight
     * of items selected by the gene
     * @return double - gene's total fitness value
     */
    private fun evalGene(gene: String): Double {
        var total_weight = 0.0
        var total_value = 0.0
        var fitness_value = 0.0
        var difference = 0.0
        var c = '0'

        // Get total_weight associated with items selected by this gene
        for (j in 0 until number_of_items) {
            c = gene[j]
            // If chromosome is a '1', add corresponding item position's
            // weight to total weight
            if (c == '1') {
                total_weight = total_weight + weight_of_items[j]
                total_value = total_value + value_of_items[j]
            }
        }
        // Check if gene's total weight is less than knapsack capacity
        difference = knapsack_capacity - total_weight
        if (difference >= 0) {
            // This is acceptable; calculate a fitness_value
            // Otherwise, fitness_value remains 0 (default), since knapsack
            // cannot hold all items selected by gene
            // Fitness value is simply total value of acceptable permutation,
            // and for unacceptable permutation is set to '0'
            fitness_value = total_value
        }

        // Return fitness value
        return fitness_value
    }

    /**
     * Makes a population by filling population ArrayList with strings of
     * length number_of_items, each element a gene of randomly generated
     * chromosomes (1s and 0s)
     */
    private fun makePopulation() {
        for (i in 0 until population_size) {
            // Calls makeGene() once for each element position
            population.add(makeGene())
        }
    }

    /**
     * Generates a single gene, a random String of 1s and 0s
     * @return String - a randomly generated gene
     */
    private fun makeGene(): String {

        // Stringbuilder builds gene, one chromosome (1 or 0) at a time
        val gene = StringBuilder(number_of_items)

        // Each chromosome
        var c: Char

        // Loop creating gene
        for (i in 0 until number_of_items) {
            c = '0'
            val rnd = Math.random()
            // If random number is greater than 0.5, chromosome is '1'
            // If random number is less than 0.5, chromosome is '0'
            if (rnd > 0.5) {
                c = '1'
            }
            // Append chromosome to gene
            gene.append(c)
        }
        // Stringbuilder object to string; return
        return gene.toString()
    }

    /**
     * Collects user input to be used as parameters for knapsack problem
     */
    private fun getInput() {

        // Hold user input, line by line
        var input: String

        // Initialize console for user input
        val c = System.console()
        if (c == null) {
            System.err.println("No console.")
            System.exit(1)
        }

        // Number of items
        input = c!!.readLine("Enter the number of items: ")
        if (isInteger(input)) {
            number_of_items = Integer.parseInt(input)
        } else {
            println("Not a number. Please try again.")
            System.exit(1)
        }

        // Value and weight of each item
        for (i in 0 until number_of_items) {
            input = c.readLine("Enter the value of item " + (i + 1) + ": ")
            if (isDouble(input)) {
                value_of_items.add(java.lang.Double.parseDouble(input))
            } else {
                println("Not a number. Please try again.")
                System.exit(1)
            }

            input = c.readLine("Enter the weight of item " + (i + 1) + ": ")
            if (isDouble(input)) {
                weight_of_items.add(java.lang.Double.parseDouble(input))
            } else {
                println("Not a number. Please try again.")
                System.exit(1)
            }
        }

        // Capacity of knapsack
        input = c.readLine("Enter the knapsack capacity: ")
        if (isInteger(input)) {
            knapsack_capacity = Integer.parseInt(input).toDouble()
        } else {
            println("Not a number. Please try again.")
            System.exit(1)
        }

        // Population size
        input = c.readLine("Enter the population size: ")
        if (isInteger(input)) {
            population_size = Integer.parseInt(input)
        } else {
            println("Not a number. Please try again.")
            System.exit(1)
        }

        // Maximum number of generations
        input = c.readLine("Enter the maximum number of generations: ")
        if (isInteger(input)) {
            maximum_generations = Integer.parseInt(input)
        } else {
            println("Not a number. Please try again.")
            System.exit(1)
        }

        // Crossover probability
        input = c.readLine("Enter the crossover probability: ")
        if (isDouble(input)) {
            prob_crossover = java.lang.Double.parseDouble(input)
        } else {
            println("Not a number. Please try again.")
            System.exit(1)
        }

        // Mutation probability
        input = c.readLine("Enter the mutation probability: ")
        if (isDouble(input)) {
            prob_mutation = java.lang.Double.parseDouble(input)
        } else {
            println("Not a number. Please try again.")
            System.exit(1)
        }
    }

    companion object {

        /**
         * Main method
         */
        @JvmStatic
        fun main(args: Array<String>) {

            // Check for command line argument output_filename
            // If filename present, redirect all System.out to file
            if (args.size == 1) {
                try {
                    val file_name = File(args[0])
                    if (file_name.exists()) {
                        file_name.delete()
                    }
                    val fos = FileOutputStream(file_name, true)
                    val ps = PrintStream(fos)
                    System.setOut(ps)
                } catch (e: FileNotFoundException) {
                    println("Problem with output file")
                }
            }

            // Construct KnapsackProblem instance and pass control
            val knap = KnapsackProblem()
        }

        /**
         * Determines if input string can be converted to integer
         * @param String - string to be checked
         * @return boolean - whether or not string can be converted
         */
        fun isInteger(str: String): Boolean {
            try {
                Integer.parseInt(str)
            } catch (e: NumberFormatException) {
                return false
            }

            return true
        }

        /**
         * Determines if input string can be converted to double
         * @param String - string to be checked
         * @return boolean - whether or not string can be converted
         */
        fun isDouble(str: String): Boolean {
            try {
                java.lang.Double.parseDouble(str)
            } catch (e: NumberFormatException) {
                return false
            }

            return true
        }
    }
} // KnapsackProblem